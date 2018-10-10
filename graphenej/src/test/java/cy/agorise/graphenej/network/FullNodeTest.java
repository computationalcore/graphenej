package cy.agorise.graphenej.network;

import junit.framework.Assert;

import org.junit.Test;

public class FullNodeTest {

    @Test
    public void testFullNodeComparable(){
        FullNode nodeA = new FullNode("wss://dummy");
        FullNode nodeB = new FullNode("wss://dummy");
        FullNode nodeC = new FullNode("wss://dummy");
        nodeA.addLatencyValue(100);
        nodeB.addLatencyValue(200);
        nodeC.addLatencyValue(100);
        Assert.assertTrue("Makes sure the node nodeA.compareTo(nodeB) returns a negative value", nodeA.compareTo(nodeB) < 0);
        Assert.assertTrue("Makes sure nodeA.compareTo(nodeB) returns zero", nodeA.compareTo(nodeC) == 0);
        Assert.assertTrue("Makes sure nodeB.compareTo(nodeA) returns a positive value", nodeB.compareTo(nodeA) > 0);
    }

    @Test
    public void testFullNodeAverageLatency(){
        FullNode fullNode = new FullNode("wss://dummy");
        fullNode.getLatencyAverage().setAlpha(0.5);
        fullNode.addLatencyValue(100);
        Assert.assertEquals(100.0, fullNode.getLatencyValue());
        fullNode.addLatencyValue(50);
        Assert.assertEquals(75.0, fullNode.getLatencyValue());
    }
}
