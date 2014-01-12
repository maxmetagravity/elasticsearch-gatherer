package org.xbib.elasticsearch.gatherer.standard;

import org.testng.annotations.Test;
import org.xbib.elasticsearch.gatherer.AbstractNodeTest;

public class StandardGathererTests extends AbstractNodeTest {

    @Test
    public void gathererTest() throws Exception {
        startNode("2");
        stopNode("2");
    }

}
