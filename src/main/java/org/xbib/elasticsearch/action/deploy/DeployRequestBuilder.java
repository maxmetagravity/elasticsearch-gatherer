
package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.internal.InternalClusterAdminClient;

import java.io.IOException;

public class DeployRequestBuilder extends NodesOperationRequestBuilder<DeployRequest, DeployResponse, DeployRequestBuilder> {

    public DeployRequestBuilder(ClusterAdminClient clusterClient) {
        super((InternalClusterAdminClient) clusterClient, new DeployRequest());
    }

    @Override
    protected void doExecute(ActionListener<DeployResponse> listener) {
        ((ClusterAdminClient) client).execute(DeployAction.INSTANCE, request, listener);
    }

    @Override
    public DeployRequest request() {
        return this.request;
    }

    public DeployRequestBuilder setName(String name) throws IOException {
        request.setName(name);
        return this;
    }

    public DeployRequestBuilder setPath(String path) throws IOException {
        request.setPath(path);
        return this;
    }
}
