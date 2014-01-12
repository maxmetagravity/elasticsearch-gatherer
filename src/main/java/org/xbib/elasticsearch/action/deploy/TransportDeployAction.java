
package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.support.nodes.TransportNodesOperationAction;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.plugin.gatherer.GathererPlugin;
import org.xbib.io.StreamUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class TransportDeployAction extends TransportNodesOperationAction<DeployRequest, DeployResponse, DeployNodeRequest, DeployNodeResponse> {

    private final Environment environment;

    private final DeployService deployService;

    @Inject
    public TransportDeployAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
                                 ClusterService clusterService, TransportService transportService,
                                 Environment environment, DeployService deployService) {
        super(settings, clusterName, threadPool, clusterService, transportService);
        this.environment = environment;
        this.deployService = deployService;
    }

    @Override
    protected String transportAction() {
        return DeployAction.NAME;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.MANAGEMENT;
    }

    @Override
    protected DeployRequest newRequest() {
        return new DeployRequest();
    }

    @Override
    protected DeployResponse newResponse(DeployRequest request, AtomicReferenceArray nodesResponses) {
        return new DeployResponse();
    }

    @Override
    protected DeployNodeRequest newNodeRequest() {
        return new DeployNodeRequest();
    }

    @Override
    protected DeployNodeRequest newNodeRequest(String nodeId, DeployRequest request) {
        return new DeployNodeRequest(nodeId, request);
    }

    @Override
    protected DeployNodeResponse newNodeResponse() {
        return new DeployNodeResponse();
    }

    @Override
    protected DeployNodeResponse nodeOperation(DeployNodeRequest request) throws ElasticSearchException {
        String name = request.getRequest().getName();
        if (name == null) {
            throw new ElasticSearchException("no name given");
        }
        String path = request.getRequest().getPath();
        if (path == null) {
            throw new ElasticSearchException("no path given");
        }
        BytesReference ref = request.getRequest().getBytes();
        if (ref == null || ref.length() == 0) {
            throw new ElasticSearchException("no bytes in request");
        }
        // place all deployments under gatherer to avoid overwriting of other plugins
        File dir = new File(environment.pluginsFile(), GathererPlugin.NAME + "/" + name);
        if (dir.exists()) {
            throw new ElasticSearchException("refusing cowardly to overwrite existing path: " + dir.getAbsolutePath());
        }
        try {
            dir.mkdirs();
            File f = new File(path); // just to get file name
            File target = new File(dir, f.getName());
            logger.info("deploying to {}", target.getAbsolutePath());
            FileOutputStream out = new FileOutputStream(target);
            InputStream in = new ByteArrayInputStream(ref.array());
            StreamUtil.copy(in, out);
            in.close();
            out.close();
            // deploy service knows how to unpack archive and add jars to class path
            deployService.add(name, target.getAbsolutePath());
            // TODO set success result in DeployNodeResponse
        } catch (IOException e) {
            throw new ElasticSearchException(e.getMessage());
        }
        DeployNodeResponse response = new DeployNodeResponse();
        return response;
    }

    @Override
    protected boolean accumulateExceptions() {
        return true;
    }

}
