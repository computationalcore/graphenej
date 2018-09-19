package cy.agorise.graphenej.network;

import java.util.List;

/**
 * Interface used to describe the high level characteristics of a class that will
 * hold and manage a list of {@link FullNode} instances.
 *
 * The idea is that the class implementing this interface should provide node instances
 * and thus URLs for the {@link cy.agorise.graphenej.api.android.NetworkService} with
 * different sorting heuristics.
 */
public interface NodeProvider {

    /**
     * Returns the node with the best characteristics. Returns null if there is no {@link FullNode}
     * @return          A FullNode instance
     */
    FullNode getBestNode();

    /**
     * Adds a new node to the queue
     * @param fullNode  {@link FullNode} instance to add.
     */
    void addNode(FullNode fullNode);

    /**
     * Updates the rating of a specific node that is already in the NodeProvider
     * @param fullNode
     */
    boolean updateNode(FullNode fullNode);

    /**
     * Returns an ordered list of {@link FullNode} instances.
     * @return
     */
    List<FullNode> getSortedNodes();
}
