package org.xbib.elasticsearch.action.gatherer;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.nodes.TransportNodesOperationAction;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.hppc.ObjectOpenHashSet;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.service.NodeService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.gatherer.GathererConstants;

import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.elasticsearch.common.collect.Sets.newTreeSet;

public class TransportGathererAction extends TransportNodesOperationAction<GathererRequest, GathererResponse, GathererNodeRequest, GathererNodeResponse> {

    private final NodeService nodeService;

    private GathererRequest request;

    @Inject
    public TransportGathererAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
                                    ClusterService clusterService, TransportService transportService,
                                    NodeService nodeService) {
        super(settings, clusterName, threadPool, clusterService, transportService);
        this.nodeService = nodeService;
    }

    @Override
    protected String transportAction() {
        return GathererAction.NAME;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.MANAGEMENT;
    }

    @Override
    protected GathererRequest newRequest() {
        return new GathererRequest();
    }

    @Override
    protected GathererResponse newResponse(GathererRequest request, AtomicReferenceArray nodesResponses) {
        return null;
    }

    @Override
    protected GathererNodeRequest newNodeRequest() {
        return new GathererNodeRequest();
    }

    @Override
    protected GathererNodeRequest newNodeRequest(String nodeId, GathererRequest request) {
        return new GathererNodeRequest(nodeId, request);
    }

    @Override
    protected GathererNodeResponse newNodeResponse() {
        return new GathererNodeResponse();
    }

    @Override
    protected GathererNodeResponse nodeOperation(GathererNodeRequest request) throws ElasticSearchException {
        GathererNodeResponse response = new GathererNodeResponse();
        return response;
    }

    @Override
    protected boolean accumulateExceptions() {
        return false;
    }

    @Override
    protected void doExecute(GathererRequest request, ActionListener<GathererResponse> listener) {
        this.request = request;
        super.doExecute(request, listener);
    }

    @Override
    protected String[] filterNodeIds(DiscoveryNodes nodes, String[] nodesIds) {
        // select one node with least system load
        if (request.getLeastBusy()) {
            // filter out all nodes that have gatherer load set
            SortedSet<WeightedDiscoveryNode> set = newTreeSet();
            for (DiscoveryNode node : nodes) {
                String s = node.attributes().get(GathererConstants.GATHERER_LOAD);
                if (s != null) {
                    set.add(new WeightedDiscoveryNode(node, s));
                }
            }
            return new String[] { set.first().node.getId() };
        } else if (request.getMinimumLength()) {
            // filter out all nodes that have gatherer queue length set
            SortedSet<WeightedDiscoveryNode> set = newTreeSet();
            for (DiscoveryNode node : nodes) {
                String s = node.attributes().get(GathererConstants.GATHERER_LENGTH);
                if (s != null) {
                    set.add(new WeightedDiscoveryNode(node, s));
                }
            }
            return new String[] { set.first().node.getId() };
        } else {
            ObjectOpenHashSet<String> filteredNodesIds = new ObjectOpenHashSet<String>();
            for (DiscoveryNode node : nodes) {
                filteredNodesIds.add(node.getId());
            }
            return filteredNodesIds.toArray(String.class);
        }
    }

    class WeightedDiscoveryNode implements Comparable<WeightedDiscoveryNode> {

        DiscoveryNode node;
        Double weight;

        WeightedDiscoveryNode(DiscoveryNode node, String weight) {
            this.node = node;
            this.weight = weight != null ? Double.parseDouble(weight) : Double.MAX_VALUE;
        }

        @Override
        public int compareTo(WeightedDiscoveryNode otherNode) {
            return weight.compareTo(otherNode.weight);
        }
    }

}
