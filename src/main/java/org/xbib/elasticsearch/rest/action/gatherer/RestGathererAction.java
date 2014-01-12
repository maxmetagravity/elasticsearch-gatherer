package org.xbib.elasticsearch.rest.action.gatherer;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

public class RestGathererAction extends BaseRestHandler {

    @Inject
    public RestGathererAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(RestRequest.Method.GET, "/_gatherer", this);
    }

    @Override
    public void handleRequest(RestRequest request, RestChannel channel) {
        // TODO

    }
}
