
package org.xbib.elasticsearch.gatherer.support;

import java.io.IOException;
import java.util.Map;

/**
 * A structured object is composed by an object data source together with
 * meta data about the object.
 */
public interface IndexableObject extends ControlKeys, Comparable<IndexableObject> {

    IndexableObject optype(String optype);

    String optype();

    IndexableObject index(String index);

    String index();

    IndexableObject type(String type);

    String type();

    IndexableObject id(String id);

    String id();

    IndexableObject meta(String key, String value);

    String meta(String key);

    IndexableObject source(Map<String, Object> source);

    Map source();

    String build() throws IOException;

    boolean isEmpty();

}
