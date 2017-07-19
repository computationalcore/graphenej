package de.bitsharesmunich.graphenej.api.android;

import java.util.ArrayList;
import java.util.List;

import de.bitsharesmunich.graphenej.api.BaseGrapheneHandler;
import de.bitsharesmunich.graphenej.api.SubscriptionMessagesHub;
import de.bitsharesmunich.graphenej.errors.RepeatedRequestIdException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;

/**
 * Created by nelson on 6/26/17.
 */

public class NodeConnection {
    /**
     * List of URLs of the nodes
     */
    private List<String> mUrlList;
    /**
     * Index of the current node from the list
     */
    private int mUrlIndex;
    private WebsocketWorkerThread mThread;
    private SubscriptionMessagesHub mMessagesHub;
    private long requestCounter = SubscriptionMessagesHub.MANUAL_SUBSCRIPTION_ID + 1;

    private static NodeConnection instance;

    /*
     * Ger the instance of the NodeConnection which is inteded to be used as a Singleton.
     */
    public static NodeConnection getInstance(){
        if(instance == null){
            instance = new NodeConnection();
        }
        return instance;
    }

    public NodeConnection(){
        this.mUrlList = new ArrayList<>();
    }

    /**
     * Add a websocket URL node that will be added to the list used at node hop scheme.
     *
     * @param url: URL of the node
     */
    public void addNodeUrl(String url){
        this.mUrlList.add(url);
    }

    /**
     * Add a list of websocket URL nodes that will be added to the current list and
     * be used at node hop scheme.
     *
     * @param urlList: List of URLs of the nodes
     */
    public void addNodeUrls(List<String> urlList){
        List<String> newList = new ArrayList<String>(mUrlList);
        newList.addAll(urlList);
    }

    /**
     * Get the list of websocket URL nodes.
     *
     * @return List of URLs of the nodes
     */
    public List<String> getNodeUrls(){
        return this.mUrlList;
    }

    /**
     * Clear list of websocket URL nodes.
     */
    public void clearNodeList(){
        this.mUrlList.clear();
    }

    /**
     * Method that will try to connect to one of the nodes. If the connection fails
     * a subsequent call to this method will try to connect with the next node in the
     * list if there is one.
     */
    public void connect(String user, String password, boolean subscribe, WitnessResponseListener errorListener) {
        if(this.mUrlList.size() > 0){
            mThread = new WebsocketWorkerThread(this.mUrlList.get(mUrlIndex));
            mUrlIndex = mUrlIndex + 1 % this.mUrlList.size();

            mMessagesHub = new SubscriptionMessagesHub(user, password, subscribe, errorListener);
            mThread.addListener(mMessagesHub);
            mThread.start();
        }
    }

    /**
     *  Add the API Handler to the node.
     */
    public void addRequestHandler(BaseGrapheneHandler handler) throws RepeatedRequestIdException {
        handler.setRequestId(requestCounter);
        requestCounter++;
        mMessagesHub.addRequestHandler(handler);
    }
}
