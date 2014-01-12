package org.xbib.elasticsearch.action.gatherer;

import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

public class GathererAction extends ClusterAction<GathererRequest, GathererResponse, GathererRequestBuilder> {

    public static final GathererAction INSTANCE = new GathererAction();

    public static final String NAME = "org.xbib.elasticsearch.action.gatherer";

    private GathererAction() {
        super(NAME);
    }

    @Override
    public GathererRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new GathererRequestBuilder(client);
    }

    @Override
    public GathererResponse newResponse() {
        return new GathererResponse();
    }
}
