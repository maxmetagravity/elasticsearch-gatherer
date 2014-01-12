package org.xbib.elasticsearch.action.gatherer;

import org.elasticsearch.action.support.nodes.NodesOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class GathererRequest extends NodesOperationRequest<GathererRequest> {

    private boolean leastBusy;

    private boolean minimumLength;

    public GathererRequest() {
    }

    public GathererRequest(String... nodeIds) {
        super(nodeIds);
    }

    public GathererRequest setLeastBusy(boolean leastBusy) {
        this.leastBusy = leastBusy;
        return this;
    }

    public boolean getLeastBusy() {
        return leastBusy;
    }

    public GathererRequest setMinimumLength(boolean minimumLength) {
        this.minimumLength = minimumLength;
        return this;
    }

    public boolean getMinimumLength() {
        return minimumLength;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }

}
