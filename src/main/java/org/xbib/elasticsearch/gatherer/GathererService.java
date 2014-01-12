package org.xbib.elasticsearch.gatherer;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.service.NodeService;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.util.concurrent.EsExecutors.daemonThreadFactory;

/**
 * The gatherer service manages the gatherer registry.
 *
 * Gatherers are announced to the node attributes so they can be seen by node discovery.
 */
public class GathererService extends AbstractLifecycleComponent<GathererService> {

    private final GathererRegistry registry;

    private final NodeService nodeService;

    private final OperatingSystemMXBean operatingSystemMXBean;

    @Inject
    public GathererService(Settings settings,
                           GathererRegistry registry,
                           NodeService nodeService) {
        super(settings);
        this.registry = registry;
        this.nodeService = nodeService;
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    }

    public GathererRegistry getRegistry() {
        return registry;
    }

    @Override
    protected void doStart() throws ElasticSearchException {

        // announce default gatherers
        announceGatherers();

        // the load updater task
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory(settings, "gatherer_load_watcher"));
        executorService.scheduleAtFixedRate(new Thread() {
            public void run() {
                double load = operatingSystemMXBean.getSystemLoadAverage();
                nodeService.putAttribute(GathererConstants.GATHERER_LOAD, Double.toString(load));
            }
        }, 1L, 1L, TimeUnit.MINUTES);

        // the queue length updater task
        executorService.scheduleAtFixedRate(new Thread() {
            public void run() {
                int length = 0; // TODO getPendingJobs();
                nodeService.putAttribute(GathererConstants.GATHERER_LENGTH, Integer.toString(length));
            }
        }, 1L, 5L, TimeUnit.SECONDS);

        logger.info("started");
    }

    @Override
    protected void doStop() throws ElasticSearchException {
        nodeService.removeAttribute(GathererConstants.GATHERER_MODULES);
        nodeService.removeAttribute(GathererConstants.GATHERER_LOAD);
        nodeService.removeAttribute(GathererConstants.GATHERER_LENGTH);
    }

    @Override
    protected void doClose() throws ElasticSearchException {
    }

    /**
     * Get all gatherers in the registry and announce them to the node service.
     */
    public void announceGatherers() {
        nodeService.putAttribute(GathererConstants.GATHERER_MODULES,
                Strings.collectionToCommaDelimitedString(registry.getGatherers().keySet()));
    }

}
