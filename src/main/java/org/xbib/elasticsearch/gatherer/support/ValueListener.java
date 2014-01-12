
package org.xbib.elasticsearch.gatherer.support;

import java.io.IOException;
import java.util.List;

/**
 * Value listener interface for receiving a stream of key/value attributes,
 * like a number of rows of columns.
 */
public interface ValueListener {

    /**
     * Set the key names for the values
     *
     * @param keys the keys
     * @return this ValueListener
     */
    ValueListener keys(List<String> keys);

    /**
     * Receive values for the keys
     *
     * @param values the values
     * @return this ValueListener
     * @throws java.io.IOException
     */
    ValueListener values(List<? extends Object> values) throws IOException;

    /**
     * Reset the key/value configuration
     *
     * @return this value listener
     * @throws java.io.IOException
     */
    ValueListener reset() throws IOException;

}
