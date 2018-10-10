package cy.agorise.graphenej.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class LatencyNodeProvider implements NodeProvider {
    private final String TAG = "LatencyNodeProvider";
    private PriorityQueue<FullNode> mFullNodeHeap;

    public LatencyNodeProvider(){
        mFullNodeHeap = new PriorityQueue<>();
    }

    @Override
    public FullNode getBestNode() {
        return mFullNodeHeap.peek();
    }

    @Override
    public void addNode(FullNode fullNode) {
        mFullNodeHeap.add(fullNode);
    }

    @Override
    public boolean updateNode(FullNode fullNode) {
        mFullNodeHeap.remove(fullNode);
        return mFullNodeHeap.offer(fullNode);
    }

    /**
     * Updates an existing node with the new latency value.
     *
     * @param fullNode  Existing full node instance
     * @param latency   New latency measurement
     * @return          True if the node priority was updated successfully
     */
    public boolean updateNode(FullNode fullNode, int latency){
        if(mFullNodeHeap.remove(fullNode)){
            fullNode.addLatencyValue(latency);
            return mFullNodeHeap.add(fullNode);
        }else{
            return false;
        }
    }

    @Override
    public List<FullNode> getSortedNodes() {
        FullNode[] nodeArray = mFullNodeHeap.toArray(new FullNode[mFullNodeHeap.size()]);
        ArrayList<FullNode> nodeList = new ArrayList<>();
        for(FullNode fullNode : nodeList){
            if(fullNode != null){
                nodeList.add(fullNode);
            }
        }
        Collections.sort(nodeList);
        return Arrays.asList(nodeArray);
    }
}
