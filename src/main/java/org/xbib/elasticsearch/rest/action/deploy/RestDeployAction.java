
package org.xbib.elasticsearch.rest.action.deploy;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.xbib.elasticsearch.action.deploy.DeployAction;
import org.xbib.elasticsearch.action.deploy.DeployRequest;
import org.xbib.elasticsearch.action.deploy.DeployRequestBuilder;
import org.xbib.elasticsearch.action.deploy.DeployResponse;

import java.io.IOException;

import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.support.RestXContentBuilder.restContentBuilder;

public class RestDeployAction extends BaseRestHandler {

    @Inject
    public RestDeployAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(RestRequest.Method.POST, "/_deploy", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        try {
            DeployRequestBuilder deployRequestBuilder = new DeployRequestBuilder(client.admin().cluster())
                    .setName(request.param("name"))
                    .setPath(request.param("path"));
            final DeployRequest deployRequest = deployRequestBuilder.request();
            client.admin().cluster().execute(DeployAction.INSTANCE, deployRequest, new ActionListener<DeployResponse>() {

                @Override
                public void onResponse(DeployResponse deployNodeResponses) {
                    try {
                        final XContentBuilder builder = restContentBuilder(request);
                        builder.startObject()
                                .field("deploy", true)
                                .endObject();
                        channel.sendResponse(new XContentRestResponse(request, OK, builder));
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    try {
                        channel.sendResponse(new XContentThrowableRestResponse(request, e));
                    } catch (IOException e1) {
                        logger.error("Failed to send failure response", e1);
                    }
                }
            });

        } catch (Throwable ex) {
            try {
                logger.error(ex.getMessage(), ex);
                channel.sendResponse(new XContentThrowableRestResponse(request, ex));
            } catch (Exception ex2) {
                logger.error(ex2.getMessage(), ex2);
            }
        }
    }
}
