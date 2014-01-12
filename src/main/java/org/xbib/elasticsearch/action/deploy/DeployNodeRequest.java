
package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.action.support.nodes.NodeOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class DeployNodeRequest extends NodeOperationRequest {

    private DeployRequest request;

    DeployNodeRequest() {
    }

    DeployNodeRequest(String nodeId, DeployRequest request) {
        super(request, nodeId);
        this.request = request;
    }

    public DeployRequest getRequest() {
        return request;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        request = new DeployRequest();
        request.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        request.writeTo(out);
    }

}
