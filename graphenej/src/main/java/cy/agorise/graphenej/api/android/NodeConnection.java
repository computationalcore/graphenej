package cy.agorise.graphenej.api.android;

import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.api.BaseGrapheneHandler;
import cy.agorise.graphenej.api.SubscriptionMessagesHub;
import cy.agorise.graphenej.errors.RepeatedRequestIdException;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.models.BaseResponse;

/**
 *  Class used to encapsulate all connections that should be done to a node (with node hop support).
 *
 *  This class is intended to be used as a central broker for all full node API requests. It should
 *  be used as a singleton under an application.
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
    private NodeErrorListener mErrorListener;

    /**
     * Singleton instance
     */
    private static NodeConnection instance;

    private String mUser;
    private String mPassword;
    private boolean mSubscribe;

    /*
     * Get the instance of the NodeConnection which is intended to be used as a Singleton.
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
     * Add a WebSocket URL node that will be added to the list used at node hop scheme.
     *
     * @param url: URL of the node
     */
    public void addNodeUrl(String url){
        this.mUrlList.add(url);
    }

    /**
     * Add a list of WebSocket URL nodes that will be added to the current list and
     * be used at node hop scheme.
     *
     * @param urlList: List of URLs of the nodes
     */
    public void addNodeUrls(List<String> urlList){
        mUrlList.addAll(urlList);
    }

    /**
     * Get the list of WebSocket URL nodes.
     *
     * @return List of URLs of the nodes
     */
    public List<String> getNodeUrls(){
        return this.mUrlList;
    }

    /**
     * Clear list of WebSocket URL nodes.
     */
    public void clearNodeList(){
        this.mUrlList.clear();
    }

    private NodeErrorListener mInternalErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("NodeConnect Error. Msg: "+error);
            mUrlIndex++;
            connect(mUser, mPassword, mSubscribe, mErrorListener);
        }
    };

    /**

     */
    /**
     * Method that will try to connect to one of the nodes. If the connection fails
     * a subsequent call to this method will try to connect with the next node in the
     * list if there is one.
     *
     * @param   user            user credential used for restricted requested that needed to be
     *                          logged
     * @param   password        password credential used for restricted requested that needed to be
     *                          logged
     * @param   subscribe       if the node should be subscribed to the node
     * @param   errorListener   a class implementing the WitnessResponseListener interface. This
     *                          should be implemented by the party interested in being notified
     *                          about the failure of the desired broadcast operation.
     */
    public void connect(String user, String password, boolean subscribe, NodeErrorListener errorListener) {
        if(mUrlList.size() > 0){
            if(mUrlIndex < mUrlList.size()){
                System.out.println("Connecting to: "+ this.mUrlList.get(mUrlIndex));
                mUser = user;
                mPassword = password;
                mSubscribe = subscribe;
                mErrorListener = errorListener;
                mThread = new WebsocketWorkerThread(this.mUrlList.get(mUrlIndex), mInternalErrorListener);

                mMessagesHub = new SubscriptionMessagesHub(user, password, subscribe, mInternalErrorListener);
                mThread.addListener(mMessagesHub);
                mThread.start();
            }else{
                errorListener.onError(new BaseResponse.Error("Can't connect, ran out of URLs!"));
            }
        }else{
            errorListener.onError(new BaseResponse.Error("Can't connect, missing URL"));
        }
    }

    /**
     * Add the API Handler to the node.
     *
     * @param   handler request handler to be added to the connection
     * @throws  RepeatedRequestIdException
     */
    public void addRequestHandler(BaseGrapheneHandler handler) throws RepeatedRequestIdException {
        handler.setRequestId(requestCounter);
        requestCounter++;
        mMessagesHub.addRequestHandler(handler);
    }
}
