
package org.xbib.elasticsearch.gatherer.support;

import org.elasticsearch.common.xcontent.json.JsonXContent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

/**
 * The SimpleValueListener class consumes values
 */
public class SimpleValueListener<O> implements ValueListener {

    /**
     * The current structured object
     */
    private IndexableObject current;
    /**
     * The object before the current object
     */
    private IndexableObject prev;

    /**
     * The keys of the values. They are examined for the Elasticsearch index
     * attributes.
     */
    private List<String> keys;
    /**
     * The delimiter between the key names in the path, used for string split.
     * Should not be modified.
     */
    private char delimiter = '.';

    public SimpleValueListener delimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Just some syntactic sugar.
     *
     * @return this value listener
     * @throws java.io.IOException
     */
    public SimpleValueListener begin() throws IOException {
        return this;
    }

    /**
     * Set the keys.
     *
     * @param keys the keys
     * @return this value listener
     */
    @Override
    public SimpleValueListener keys(List<String> keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Receive values.
     *
     * @param values the values
     * @return this value listener
     * @throws java.io.IOException
     */
    @Override
    public SimpleValueListener values(List<? extends Object> values) throws IOException {
        boolean hasSource = false;
        if (current == null) {
            current = newObject();
        }
        if (prev == null) {
            prev = newObject();
        }
        // execute meta operations on pseudo columns
        for (int i = 0; i < values.size(); i++) {
            // v may be null
            Object o = values.get(i);
            if (o == null) {
                continue;
            }
            String v = o.toString();
            // JAVA7: string switch
            String k = keys.get(i);
            map(k, v, current);
            if (IndexableObject.SOURCE.equals(k)) {
                hasSource = true;
            }
        }
        if (hasSource) {
            end(current);
            current = newObject();
            return this;
        }
        // switch to next structured object if current is not equal to previous
        if (!current.equals(prev) || current.isEmpty()) {
            prev.source(current.source()); // "steal" source
            end(prev); // here, the element is being prepared for bulk indexing
            prev = current;
            current = newObject();
        }
        // create current object from values by sequentially merging the values
        for (int i = 0; i < keys.size(); i++) {
            Map map = null;
            try {
                map = JsonXContent.jsonXContent.createParser(values.get(i).toString()).mapAndClose();
            } catch (Exception e) {
                // ignore
            }
            Map m = merge(current.source(), keys.get(i), map != null && map.size() > 0 ? map : values.get(i));
            current.source(m);
        }
        return this;
    }

    protected void map(String k, String v, IndexableObject current) throws IOException {
        if (IndexableObject.JOB.equals(k)) {
            current.meta(IndexableObject.JOB, v);
        } else if (IndexableObject.OPTYPE.equals(k)) {
            current.optype(v);
        } else if (IndexableObject.INDEX.equals(k)) {
            current.index(v);
        } else if (IndexableObject.TYPE.equals(k)) {
            current.type(v);
        } else if (IndexableObject.ID.equals(k)) {
            current.id(v);
        } else if (IndexableObject.VERSION.equals(k)) {
            current.meta(IndexableObject.VERSION, v);
        } else if (IndexableObject.ROUTING.equals(k)) {
            current.meta(IndexableObject.ROUTING, v);
        } else if (IndexableObject.PERCOLATE.equals(k)) {
            current.meta(IndexableObject.PERCOLATE, v);
        } else if (IndexableObject.PARENT.equals(k)) {
            current.meta(IndexableObject.PARENT, v);
        } else if (IndexableObject.TIMESTAMP.equals(k)) {
            current.meta(IndexableObject.TIMESTAMP, v);
        } else if (IndexableObject.TTL.equals(k)) {
            current.meta(IndexableObject.TTL, v);
        } else if (IndexableObject.SOURCE.equals(k)) {
            current.source(JsonXContent.jsonXContent.createParser(v).mapAndClose());
        }
    }

    /**
     * End of values.
     *
     * @return this value listener
     * @throws java.io.IOException
     */
    public SimpleValueListener end() throws IOException {
        if (prev != null) {
            prev.source(current.source());
            end(prev);
        }
        prev = newObject();
        current = newObject();
        return this;
    }

    /**
     * The object is complete
     *
     * @param object the object
     * @return this value listener
     * @throws java.io.IOException
     */
    public SimpleValueListener end(IndexableObject object) throws IOException {
        if (object.source().isEmpty()) {
            return this;
        }
        /*if (target != null) {
            if (object.optype() == null) {
                target.index(object);
            } else if (Operations.OP_INDEX.equals(object.optype())) {
                target.index(object);
            } else if (Operations.OP_CREATE.equals(object.optype())) {
                target.create(object);
            } else if (Operations.OP_DELETE.equals(object.optype())) {
                target.delete(object);
            } else {
                throw new IllegalArgumentException("unknown optype: " + object.optype());
            }
        }*/
        return this;
    }

    /**
     * Reset this listener
     *
     * @throws java.io.IOException
     */
    @Override
    public SimpleValueListener reset() throws IOException {
        end();
        return this;
    }

    /**
     * Merge key/value pair to a map holding a JSON object. The key consists of
     * a path pointing to the value position in the JSON object. The key,
     * representing a path, is divided into head/tail. The recursion terminates
     * if there is only a head and no tail. In this case, the value is added as
     * a tuple to the map. If the head key exists, the merge process is
     * continued by following the path represented by the key. If the path does
     * not exist, a new map is created. A conflict arises if there is no map at
     * a head key position. Then, the prefix given in the path is considered
     * illegal.
     *
     * @param map   the map for the JSON object
     * @param key   the key
     * @param value the value
     */
    protected Map<String, Values<O>> merge(Map map, String key, Object value) {
        if (ControlKeys.INDEX.equals(key)
                || ControlKeys.ID.equals(key)
                || ControlKeys.TYPE.equals(key)
                || ControlKeys.PARENT.equals(key)) {
            return map;
        }
        int i = key.indexOf(delimiter);
        String index = null;
        if (i <= 0) {
            Matcher matcher = p.matcher(key);
            boolean isSequence = matcher.matches();
            String head = key;
            if (isSequence) {
                head = matcher.group(1);
                index = matcher.group(2);
            }
            if (index == null || index.isEmpty()) {
                map.put(head, new Values(map.get(head), value, isSequence));
            } else {
                if (!map.containsKey(head)) {
                    map.put(head, newLinkedList());
                }
                Object o = map.get(head);
                if (o instanceof List) {
                    List l = (List) o;
                    int j = l.isEmpty() ? -1 : l.size() - 1;
                    if (j >= 0) {
                        Map<String, Values<O>> m = (Map<String, Values<O>>) l.get(j);
                        if (!m.containsKey(index)) {
                            l.set(j, merge(m, index, value)); // append
                        } else {
                            l.add(merge(new HashMap(), index, value));
                        }
                    } else {
                        l.add(merge(new HashMap(), index, value));
                    }
                }
            }
        } else {
            String head = key.substring(0, i);
            Matcher matcher = p.matcher(head);
            boolean isSequence = matcher.matches();
            if (isSequence) {
                head = matcher.group(1);
            }
            String tail = key.substring(i + 1);
            if (map.containsKey(head)) {
                Object o = map.get(head);
                if (o instanceof Map) {
                    merge((Map<String, Values<O>>) o, tail, value);
                } else {
                    throw new IllegalArgumentException("illegal head: " + head);
                }
            } else {
                Map<String, Values<O>> m = new HashMap<String, Values<O>>();
                map.put(head, m);
                merge(m, tail, value);
            }
        }
        return map;
    }

    private final static Pattern p = Pattern.compile("^(.*)\\[(.*?)\\]$");

    /**
     * Create a new structured object
     *
     * @return a new structured object
     */
    private IndexableObject newObject() {
        return new PlainIndexableObject();
    }

}
