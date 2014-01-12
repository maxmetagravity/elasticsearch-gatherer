package org.xbib.elasticsearch.action.gatherer;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.internal.InternalClusterAdminClient;

public class GathererRequestBuilder extends NodesOperationRequestBuilder<GathererRequest, GathererResponse, GathererRequestBuilder> {

    public GathererRequestBuilder(ClusterAdminClient clusterClient) {
        super((InternalClusterAdminClient) clusterClient, new GathererRequest());
    }

    @Override
    protected void doExecute(ActionListener<GathererResponse> listener) {
        ((ClusterAdminClient) client).execute(GathererAction.INSTANCE, request, listener);
    }
}
