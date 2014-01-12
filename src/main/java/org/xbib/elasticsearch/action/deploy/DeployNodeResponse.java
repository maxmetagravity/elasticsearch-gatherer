package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.action.support.nodes.NodeOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class DeployNodeResponse extends NodeOperationResponse {

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }
}
