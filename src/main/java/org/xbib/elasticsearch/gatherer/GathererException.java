
package org.xbib.elasticsearch.gatherer;

import java.io.IOException;

public class GathererException extends IOException {

    public GathererException(String msg) {
        super(msg);
    }

    public GathererException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
