package org.xbib.elasticsearch.gatherer.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Codec {

    String getName();

    InputStream decode(InputStream in) throws IOException;

    OutputStream encode(OutputStream out) throws IOException;

}
