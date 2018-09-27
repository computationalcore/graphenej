package cy.agorise.graphenej.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import cy.agorise.graphenej.api.android.NetworkService;
import io.reactivex.subjects.PublishSubject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Class that encapsulates the node latency verification task
 */
public class NodeLatencyVerifier {
    private final String TAG = this.getClass().getName();

    private static final int DEFAULT_LATENCY_VERIFICATION_PERIOD = 5 * 1000;

    // Variable used to store the list of nodes that should be verified
    private List<FullNode> mNodeList;

    // Variable used to store the desired verification period
    private long verificationPeriod;

    // Subject used to publish the result to interested parties
    private PublishSubject<FullNode> subject = PublishSubject.create();

    private HashMap<HttpUrl, FullNode> nodeURLMap = new HashMap<>();

//    private WebSocket webSocket;

    // Map used to store the first timestamp required for a RTT (Round Trip Time) measurement.
    // If:
    //      RTT = t2 - t1
    // This map will hold the value of t1 for each one of the nodes to be measured.
    private HashMap<FullNode, Long> timestamps = new HashMap<>();

    private HashMap<String, Request> requestMap = new HashMap<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OkHttpClient client;

    public NodeLatencyVerifier(List<FullNode> nodes){
        this(nodes, DEFAULT_LATENCY_VERIFICATION_PERIOD);
    }

    public NodeLatencyVerifier(List<FullNode> nodes, long period){
        mNodeList = nodes;
        verificationPeriod = period;
    }

    /**
     * Method used to start the latency verification task.
     * <p>
     * The returning object can be used for interested parties to receive constant updates
     * regarding new latency measurements for every full node.
     * </p>
     * @return  A {@link PublishSubject} class instance.
     */
    public PublishSubject start(){
        mHandler.post(mVerificationTask);
        return subject;
    }

    /**
     * Method used to cancel the verification task.
     */
    public void stop(){
        mHandler.removeCallbacks(mVerificationTask);
    }

    /**
     * Node latency verification task.
     */
    private final Runnable mVerificationTask = new Runnable() {
        @Override
        public void run() {
            for(FullNode fullNode : mNodeList){
                long before = System.currentTimeMillis();
                timestamps.put(fullNode, before);

                // We want to reuse the same OkHttpClient instance if possible
                if(client == null) client = new OkHttpClient();

                // Same thing with the Request instance, we want to reuse them. But since
                // we might have one request per node, we keep them in a map.
                Request request;
                if(requestMap.containsKey(fullNode.getUrl())){
                    request = requestMap.get(fullNode.getUrl());
                }else{
                    // If the map had no entry for the request we want, we create one
                    // and add it to the map.
                    request = new Request.Builder().url(fullNode.getUrl()).build();
                    requestMap.put(fullNode.getUrl(), request);
                }

                String normalURL = fullNode.getUrl().replace("wss://", "https://");
                if(!nodeURLMap.containsKey(fullNode.getUrl().replace("wss://", "https://"))){
                    HttpUrl key = HttpUrl.parse(normalURL);
                    nodeURLMap.put(key, fullNode);
                }

                client.newWebSocket(request, mWebSocketListener);
            }
            mHandler.postDelayed(this, verificationPeriod);
        }
    };

    /**
     * Listener that will be called upon a server response.
     */
    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            handleResponse(webSocket, response);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            handleResponse(webSocket, response);
        }

        /**
         * Method used to handle the node's first response. The idea here is to obtain
         * the RTT (Round Trip Time) measurement and publish it using the PublishSubject.
         *
         * @param webSocket Websocket instance
         * @param response  Response instance
         */
        private void handleResponse(WebSocket webSocket, Response response){
            // Obtaining the HttpUrl instance that was previously used as a key
            HttpUrl url = webSocket.request().url();
            if(nodeURLMap.containsKey(url)){
                FullNode fullNode = nodeURLMap.get(url);
                long delay;

                if(response == null) {
                    // There is no internet connection, or the node is unreachable. We are just
                    // putting an artificial delay.
                    delay = Long.MAX_VALUE;
                } else {
                    long after = System.currentTimeMillis();
                    long before = timestamps.get(fullNode);
                    delay = after - before;
                }

                fullNode.addLatencyValue(delay);
                subject.onNext(fullNode);
            }else{
                // We cannot properly handle a response to a request whose
                // URL was not registered at the nodeURLMap. This is because without this,
                // we cannot know to which node this response corresponds. This should not happen.
                Log.e(TAG,"nodeURLMap does not contain url: "+url);
                for(HttpUrl key : nodeURLMap.keySet()){
                    Log.e(TAG,"> "+key);
                }
            }
            webSocket.close(NetworkService.NORMAL_CLOSURE_STATUS, null);
        }
    };

    /**
     * Updates the 'isConnected' attribute of a specific node.
     * @param fullNode  The node we want to update.
     */
    public void updateActiveNodeInformation(FullNode fullNode){
        for(FullNode node : mNodeList){
            if(node.equals(fullNode)){
                node.setConnected(fullNode.isConnected());
            }
        }
    }
}
