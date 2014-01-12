package org.xbib.elasticsearch.action.deploy;

import org.elasticsearch.action.support.nodes.NodesOperationRequest;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.xbib.io.FastByteArrayOutputStream;
import org.xbib.io.StreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DeployRequest extends NodesOperationRequest<DeployRequest> {

    private final ESLogger logger = ESLoggerFactory.getLogger(DeployRequest.class.getSimpleName());

    private String name;

    private String path;

    private BytesReference ref;

    public DeployRequest() {
    }

    public DeployRequest(String... nodeIds) {
        super(nodeIds);
    }

    public DeployRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public DeployRequest setPath(String path) throws IOException {
        this.path = path;
        if (path == null || path.isEmpty()) {
            throw new IOException("path not given");
        }
        File f = new File(path);
        if (!f.exists() || !f.canRead()) {
            throw new IOException("can't read from " + path);
        }
        FileInputStream in = new FileInputStream(f);
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        StreamUtil.copy(in, out);
        in.close();
        out.close();
        this.ref = out.bytes();
        logger.info("ref length = {}", ref.length());
        return this;
    }

    public String getPath() {
        return path;
    }

    public BytesReference getBytes() {
        return ref;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        this.name = in.readString();
        this.path = in.readString();
        this.ref = in.readBytesReference();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        if (name == null) {
            throw new IOException("no name was given for deploy request");
        }
        if (ref == null) {
            throw new IOException("no valid path was given for deploy request");
        }
        out.writeString(name);
        out.writeString(path);
        out.writeBytesReference(ref);
    }

}
