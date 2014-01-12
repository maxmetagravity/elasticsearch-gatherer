package org.xbib.elasticsearch.action.gatherer;

import org.elasticsearch.action.support.nodes.NodesOperationResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

public class GathererResponse extends NodesOperationResponse<GathererNodeResponse> implements ToXContent  {

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("cluster_name", getClusterName().value(), XContentBuilder.FieldCaseConversion.NONE);
        return null;
    }
}
