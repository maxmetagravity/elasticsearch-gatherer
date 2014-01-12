package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import org.xbib.classloader.uri.URIClassLoader;
import org.xbib.elasticsearch.gatherer.Gatherer;
import org.xbib.elasticsearch.gatherer.GathererService;
import org.xbib.io.StreamUtil;
import org.xbib.io.archivers.zip.ZipArchiveEntry;
import org.xbib.io.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.collect.Sets.newHashSet;

/**
 * The deploy service is a service for deploying gatherer plugin jars to a node.
 *
 * It must be initialized as a singleton.
 *
 * Gatherers are added to this service by the gatherer name and a valid path name.
 * The path name is included into a separate URI classloader and searched for
 * es-gatherer.properties for instantiating Gatherer instances.
 *
 * All gatherer instances are collected in a map with the gatherer name as key.
 *
 */
public class DeployService extends AbstractLifecycleComponent<DeployService> {

    private final URIClassLoader deployClassLoader;

    private final GathererService gathererService;

    @Inject
    public DeployService(Settings settings, GathererService gathererService) {
        super(settings);
        this.deployClassLoader = new URIClassLoader(settings.getClass().getClassLoader());
        this.gathererService = gathererService;
    }

    @Override
    protected void doStart() throws ElasticSearchException {

    }

    @Override
    protected void doStop() throws ElasticSearchException {

    }

    @Override
    protected void doClose() throws ElasticSearchException {

    }

    /**
     * Add JAR to gatherer registry
     *
     * @param name the prefix to register the gatherer package under
     * @param path the path of JAR
     */
    public void add(String name, String path) throws IOException {
        // new class loader for each gatherer
        URIClassLoader uriClassLoader = new URIClassLoader(deployClassLoader);
        // try to unpack (zip)
        tryUnpackArchive(path);
        // find all jars
        Set<URI> jars = newHashSet();
        findJars(path, jars);
        for (URI jar : jars) {
            uriClassLoader.addURI(jar);
        }
        gathererService.getRegistry().getGatherers()
                .putAll(loadGathererFromClasspath(name, uriClassLoader));
        logger.info("installed {}", path);
        for (URI uri : uriClassLoader.getURIs()) {
            logger.info("class path member {}", uri);
        }
        logger.info("registry = {}", gathererService.getRegistry());
        gathererService.announceGatherers();
    }

    private Map<String, Gatherer> loadGathererFromClasspath(String prefix, ClassLoader classLoader) {
        Map<String, Gatherer> gatherers = newHashMap();
        Enumeration<URL> gathererUrls;
        try {
            gathererUrls = classLoader.getResources("es-gatherer.properties");
        } catch (IOException e) {
            logger.warn("failed to find gatherers in classpath", e);
            return gatherers;
        }
        while (gathererUrls.hasMoreElements()) {
            URL gathererUrl = gathererUrls.nextElement();
            Properties gathererProps = new Properties();
            InputStream is = null;
            try {
                is = gathererUrl.openStream();
                gathererProps.load(is);
                Gatherer gatherer = loadGatherer(gathererProps.getProperty("gatherer"), classLoader);
                gatherers.put(prefix + "/" + gatherer.name(), gatherer);
            } catch (Exception e) {
                logger.warn("failed to load gatherer from [" + gathererUrl + "]", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return gatherers;
    }

    @SuppressWarnings("unchecked")
    private Gatherer loadGatherer(String className, ClassLoader classLoader) {
        try {
            Class<? extends Gatherer> gathererClass = (Class<? extends Gatherer>) classLoader.loadClass(className);
            try {
                return gathererClass.getConstructor(Settings.class).newInstance(settings);
            } catch (NoSuchMethodException e) {
                try {
                    return gathererClass.getConstructor().newInstance();
                } catch (NoSuchMethodException e1) {
                    throw new ElasticSearchException("No constructor for [" + gathererClass + "]. A gatherer class must " +
                            "have either an empty default constructor or a single argument constructor accepting a " +
                            "Settings instance");
                }
            }
        } catch (Exception e) {
            throw new ElasticSearchException("Failed to load gatherer class [" + className + "]", e);
        }
    }

    private void tryUnpackArchive(String path) throws IOException {
        if (path ==null) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("file does not exist: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("can not read: " + file.getAbsolutePath());
        }
        if (!file.getName().toLowerCase().endsWith(".zip")) {
            return;
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            //we check whether we need to remove the top-level folder while extracting
            //sometimes (e.g. github) the downloaded archive contains a top-level folder which needs to be removed
            boolean removeTopLevelDir = topLevelDirInExcess(zipFile);
            Enumeration<? extends ZipArchiveEntry> zipEntries = zipFile.getEntries();
            while (zipEntries.hasMoreElements()) {
                ZipArchiveEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String zipEntryName = zipEntry.getName().replace('\\', '/');
                if (removeTopLevelDir) {
                    zipEntryName = zipEntryName.substring(zipEntryName.indexOf('/'));
                }
                File target = new File(file.getParent(), zipEntryName);
                StreamUtil.copy(zipFile.getInputStream(zipEntry), new FileOutputStream(target));
            }
        } catch (Exception e) {
            logger.error("failed to extract " + file.getAbsolutePath(), e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            // remove zip file
            file.delete();
        }
    }

    private boolean topLevelDirInExcess(ZipFile zipFile) {
        //We don't rely on ZipEntry#isDirectory because it might be that there is no explicit dir
        //but the files path do contain dirs, thus they are going to be extracted on sub-folders anyway
        Enumeration<? extends ZipArchiveEntry> zipEntries = zipFile.getEntries();
        Set<String> topLevelDirNames = newHashSet();
        while (zipEntries.hasMoreElements()) {
            ZipArchiveEntry zipEntry = zipEntries.nextElement();
            String zipEntryName = zipEntry.getName().replace('\\', '/');
            int slash = zipEntryName.indexOf('/');
            //if there isn't a slash in the entry name it means that we have a file in the top-level
            if (slash == -1) {
                return false;
            }
            topLevelDirNames.add(zipEntryName.substring(0, slash));
            //if we have more than one top-level folder
            if (topLevelDirNames.size() > 1) {
                return false;
            }
        }
        return topLevelDirNames.size() == 1;
    }

    private void findJars(String path, Set<URI> jars) {
        File root = new File( path );
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }
        for (File f : list) {
            if (f.isDirectory()) {
                findJars(f.getAbsolutePath(), jars);
            } else {
                if (f.getName().endsWith(".jar")) {
                    jars.add(URI.create("file:" + f.getAbsolutePath()));
                }
            }
        }
    }

}
