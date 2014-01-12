package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

public class DeployAction extends ClusterAction<DeployRequest, DeployResponse, DeployRequestBuilder> {

    public static final DeployAction INSTANCE = new DeployAction();

    public static final String NAME = "org.xbib.elasticsearch.action.deploy";

    private DeployAction() {
        super(NAME);
    }

    @Override
    public DeployRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new DeployRequestBuilder(client);
    }

    @Override
    public DeployResponse newResponse() {
        return new DeployResponse();
    }
}
