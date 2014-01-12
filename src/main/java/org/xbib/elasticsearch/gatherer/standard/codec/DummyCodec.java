package org.xbib.elasticsearch.gatherer.standard.codec;

import org.xbib.elasticsearch.gatherer.api.Codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DummyCodec implements Codec {
    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public InputStream decode(InputStream in) throws IOException {
        return in;
    }

    @Override
    public OutputStream encode(OutputStream out) throws IOException {
        return out;
    }
}
