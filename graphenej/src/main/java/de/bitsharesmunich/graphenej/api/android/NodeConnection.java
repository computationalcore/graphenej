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
    private List<String> mUrlList;
    private int mUrlIndex;
    private WebsocketWorkerThread mThread;
    private SubscriptionMessagesHub mMessagesHub;
    private long requestCounter = SubscriptionMessagesHub.SUBCRIPTION_NOTIFICATION + 1;

    private static NodeConnection instance;

    public static NodeConnection getInstance(){
        if(instance == null){
            instance = new NodeConnection();
        }
        return instance;
    }

    public NodeConnection(){
        this.mUrlList = new ArrayList<>();
    }

    public void addNodeUrl(String url){
        this.mUrlList.add(url);
    }

    public List<String> getNodeUrls(){
        return this.mUrlList;
    }

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

    public void addRequestHandler(BaseGrapheneHandler handler) throws RepeatedRequestIdException {
        handler.setRequestId(requestCounter);
        requestCounter++;
        mMessagesHub.addRequestHandler(handler);
    }
}
