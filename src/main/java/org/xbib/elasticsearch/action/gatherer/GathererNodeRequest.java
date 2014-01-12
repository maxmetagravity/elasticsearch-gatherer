package org.xbib.elasticsearch.action.gatherer;

import org.elasticsearch.action.support.nodes.NodeOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class GathererNodeRequest extends NodeOperationRequest {

    GathererRequest request;

    GathererNodeRequest() {
    }

    GathererNodeRequest(String nodeId, GathererRequest request) {
        super(request, nodeId);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        request = new GathererRequest();
        request.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        request.writeTo(out);
    }

}
