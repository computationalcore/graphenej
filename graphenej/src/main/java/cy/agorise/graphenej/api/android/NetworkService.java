package cy.agorise.graphenej.api.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.api.ConnectionStatusUpdate;
import cy.agorise.graphenej.api.bitshares.Nodes;
import cy.agorise.graphenej.api.calls.ApiCallable;
import cy.agorise.graphenej.api.calls.GetAccounts;
import cy.agorise.graphenej.api.calls.GetRelativeAccountHistory;
import cy.agorise.graphenej.api.calls.GetRequiredFees;
import cy.agorise.graphenej.models.AccountProperties;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.Block;
import cy.agorise.graphenej.models.JsonRpcResponse;
import cy.agorise.graphenej.models.OperationHistory;
import io.reactivex.annotations.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Service in charge of maintaining a connection to the full node.
 */

public class NetworkService extends Service {
    private final String TAG = this.getClass().getName();

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    public static final String KEY_USERNAME = "key_username";

    public static final String KEY_PASSWORD = "key_password";

    public static final String KEY_REQUESTED_APIS = "key_requested_apis";

    /**
     * Constant used to pass a custom list of node URLs. This should be a simple
     * comma separated list of URLs.
     *
     * For example:
     *
     *      wss://domain1.com/ws,wss://domain2.com/ws,wss://domain3.com/ws
     */
    public static final String KEY_CUSTOM_NODE_URLS = "key_custom_node_urls";

    private final IBinder mBinder = new LocalBinder();

    private WebSocket mWebSocket;

    private int mSocketIndex;

    // Username and password used to connect to a specific node
    private String mUsername;
    private String mPassword;

    private boolean isLoggedIn = false;

    private String mLastCall;
    private long mCurrentId = 0;

    // Requested APIs passed to this service
    private int mRequestedApis;

    // Variable used to keep track of the currently obtained API accesses
    private HashMap<Integer, Integer> mApiIds = new HashMap();

    private ArrayList<String> mNodeUrls = new ArrayList<>();

    private Gson gson = new Gson();

    // Map used to keep track of outgoing request ids and its request types. This is just
    // one of two required mappings. The second one is implemented by the DeserializationMap
    // class.
    private HashMap<Long, Class> mRequestClassMap = new HashMap<>();

    // This class is used to keep track of the mapping between request classes and response
    // payload classes. It also provides a handy method that returns a Gson deserializer instance
    // suited for every response type.
    private DeserializationMap mDeserializationMap = new DeserializationMap();

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Retrieving credentials and requested API data from the shared preferences
        mUsername = pref.getString(NetworkService.KEY_USERNAME, "");
        mPassword = pref.getString(NetworkService.KEY_PASSWORD, "");
        mRequestedApis = pref.getInt(NetworkService.KEY_REQUESTED_APIS, -1);

        // If the user of the library desires, a custom list of node URLs can
        // be passed using the KEY_CUSTOM_NODE_URLS constant
        String serializedNodeUrls = pref.getString(NetworkService.KEY_CUSTOM_NODE_URLS, "");

        // Deciding whether to use an externally provided list of node URLs, or use our internal one
        if(serializedNodeUrls.equals("")){
            for(int i = 0; i < Nodes.NODE_URLS.length; i++){
                mNodeUrls.add(Nodes.NODE_URLS[i]);
            }
        }else{
            String[] urls = serializedNodeUrls.split(",");
            for(String url : urls){
                mNodeUrls.add(url);
            }
        }

        connect();
    }

    private void connect(){
        OkHttpClient client = new OkHttpClient();
        String url = mNodeUrls.get(mSocketIndex % mNodeUrls.size());
        Log.d(TAG,"Trying to connect with: "+url);
        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, mWebSocketListener);
    }

    public long sendMessage(String message){
        if(mWebSocket != null){
            if(mWebSocket.send(message)){
                Log.v(TAG,"-> " + message);
            }
        }else{
            throw new RuntimeException("Websocket connection has not yet been established");
        }
        return mCurrentId;
    }

    public long sendMessage(ApiCallable apiCallable, int requiredApi){
        int apiId = 0;
        if(requiredApi != -1 && mApiIds.containsKey(requiredApi)){
            apiId = mApiIds.get(requiredApi);
        }
        ApiCall call = apiCallable.toApiCall(apiId, ++mCurrentId);
        mRequestClassMap.put(mCurrentId, apiCallable.getClass());
        if(mWebSocket.send(call.toJsonString())){
            Log.v(TAG,"-> "+call.toJsonString());
        }
        return mCurrentId;
    }

    /**
     * Method used to inform any external party a clue about the current connectivity status
     * @return  True if the service is currently connected and logged in, false otherwise.
     */
    public boolean isConnected(){
        return mWebSocket != null && isLoggedIn;
    }

    @Override
    public void onDestroy() {
        if(mWebSocket != null)
            mWebSocket.close(NORMAL_CLOSURE_STATUS, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public NetworkService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NetworkService.this;
        }
    }

    private WebSocketListener mWebSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            mWebSocket = webSocket;

            // Notifying all listeners about the new connection status
            RxBus.getBusInstance().send(new ConnectionStatusUpdate(ConnectionStatusUpdate.CONNECTED));

            // If we're not yet logged in, we should do it now
            if(!isLoggedIn){
                ArrayList<Serializable> loginParams = new ArrayList<>();
                loginParams.add(mUsername);
                loginParams.add(mPassword);
                ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, ++mCurrentId);
                mLastCall = RPC.CALL_LOGIN;
                sendMessage(loginCall.toJsonString());
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.v(TAG,"<- "+text);
            JsonRpcResponse<?> response = gson.fromJson(text, JsonRpcResponse.class);

            // We will only handle messages that relate to the login and API accesses here.
            if(response.result != null){
                if(response.result instanceof Double || response.result instanceof Boolean){
                    if(mLastCall.equals(RPC.CALL_LOGIN)){
                        isLoggedIn = true;

                        checkNextRequestedApiAccess();
                    }else if(mLastCall.equals(RPC.CALL_DATABASE)){
                        // Deserializing integer response
                        Type IntegerJsonResponse = new TypeToken<JsonRpcResponse<Integer>>(){}.getType();
                        JsonRpcResponse<Integer> apiIdResponse = gson.fromJson(text, IntegerJsonResponse);

                        // Storing the "database" api id
                        mApiIds.put(ApiAccess.API_DATABASE, apiIdResponse.result);

                        checkNextRequestedApiAccess();
                    }else if(mLastCall.equals(RPC.CALL_HISTORY)){
                        // Deserializing integer response
                        Type IntegerJsonResponse = new TypeToken<JsonRpcResponse<Integer>>(){}.getType();
                        JsonRpcResponse<Integer> apiIdResponse = gson.fromJson(text, IntegerJsonResponse);

                        // Storing the "history" api id
                        mApiIds.put(ApiAccess.API_HISTORY, apiIdResponse.result);

                        checkNextRequestedApiAccess();
                    }else if(mLastCall.equals(RPC.CALL_NETWORK_BROADCAST)){
                        // Deserializing integer response
                        Type IntegerJsonResponse = new TypeToken<JsonRpcResponse<Integer>>(){}.getType();
                        JsonRpcResponse<Integer> apiIdResponse = gson.fromJson(text, IntegerJsonResponse);

                        // Storing the "network_broadcast" api access
                        mApiIds.put(ApiAccess.API_NETWORK_BROADCAST, apiIdResponse.result);

                        // All calls have been handled at this point
                        mLastCall = "";
                    }
                }
            }else{
                Log.w(TAG,"Error.Msg: "+response.error.message);
            }

            JsonRpcResponse parsedResponse = null;

            Class requestClass = mRequestClassMap.get(response.id);
            if(requestClass != null){
                // Removing the class entry in the map
                mRequestClassMap.remove(mCurrentId);

                // Obtaining the response payload class
                Class responsePayloadClass = mDeserializationMap.getReceivedClass(requestClass);
                Gson gson = mDeserializationMap.getGson(requestClass);
                if(responsePayloadClass == Block.class){
                    // If the response payload is a simple Block instance, we proceed to de-serialize it
                    Type GetBlockResponse = new TypeToken<JsonRpcResponse<Block>>() {}.getType();
                    parsedResponse = gson.fromJson(text, GetBlockResponse);
                }else if(responsePayloadClass == List.class){
                    // If the response payload is a List, further inquiry is required in order to
                    // determine a list of what is expected here
                    if(requestClass == GetAccounts.class){
                        // If the request call was the wrapper to the get_accounts API call, we know
                        // the response should be in the form of a JsonRpcResponse<List<AccountProperties>>
                        // so we proceed with that
                        Type GetAccountsResponse = new TypeToken<JsonRpcResponse<List<AccountProperties>>>(){}.getType();
                        parsedResponse = gson.fromJson(text, GetAccountsResponse);
                    }else if(requestClass == GetRequiredFees.class){
                        Type GetRequiredFeesResponse = new TypeToken<JsonRpcResponse<List<AssetAmount>>>(){}.getType();
                        parsedResponse = gson.fromJson(text, GetRequiredFeesResponse);
                    }else if(requestClass == GetRelativeAccountHistory.class){
                        Type RelativeAccountHistoryResponse = new TypeToken<JsonRpcResponse<List<OperationHistory>>>(){}.getType();
                        parsedResponse = gson.fromJson(text, RelativeAccountHistoryResponse);
                    }else{
                        Log.w(TAG,"Unknown request class");
                    }
                }else{
                    Log.w(TAG,"Unhandled situation");
                }
            }

            // In case the parsedResponse instance is null, we fall back to the raw response
            if(parsedResponse == null){
                parsedResponse = response;
            }

            // Broadcasting the parsed response to all interested listeners
            RxBus.getBusInstance().send(parsedResponse);
        }

        private void checkNextRequestedApiAccess(){
            if( (mRequestedApis & ApiAccess.API_DATABASE) == ApiAccess.API_DATABASE &&
                    mApiIds.get(ApiAccess.API_DATABASE) == null){
                // If we need the "database" api access and we don't yet have it

                ApiCall apiCall = new ApiCall(1, RPC.CALL_DATABASE, null, RPC.VERSION, ++mCurrentId);
                mLastCall = RPC.CALL_DATABASE;
                sendMessage(apiCall.toJsonString());
            } else if( (mRequestedApis & ApiAccess.API_HISTORY) == ApiAccess.API_HISTORY &&
                    mApiIds.get(ApiAccess.API_HISTORY) == null){
                // If we need the "history" api access and we don't yet have it

                ApiCall apiCall = new ApiCall(1, RPC.CALL_HISTORY, null, RPC.VERSION, ++mCurrentId);
                mLastCall = RPC.CALL_HISTORY;
                sendMessage(apiCall.toJsonString());
            }else if( (mRequestedApis & ApiAccess.API_NETWORK_BROADCAST) == ApiAccess.API_NETWORK_BROADCAST &&
                    mApiIds.get(ApiAccess.API_NETWORK_BROADCAST) == null){
                // If we need the "network_broadcast" api access and we don't yet have it

                ApiCall apiCall = new ApiCall(1, RPC.CALL_NETWORK_BROADCAST, null, RPC.VERSION, ++mCurrentId);
                mLastCall = RPC.CALL_NETWORK_BROADCAST;
                sendMessage(apiCall.toJsonString());
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            RxBus.getBusInstance().send(new ConnectionStatusUpdate(ConnectionStatusUpdate.DISCONNECTED));

            isLoggedIn = false;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.e(TAG,"onFailure. Exception: "+t.getClass().getName()+", Msg: "+t.getMessage());
            // Logging error stack trace
            for(StackTraceElement element : t.getStackTrace()){
                Log.e(TAG,String.format("%s#%s:%s", element.getClassName(), element.getMethodName(), element.getLineNumber()));
            }
            // Registering current status
            isLoggedIn = false;
            mCurrentId = 0;
            mApiIds.clear();

            // If there is a response, we print it
            if(response != null){
                Log.e(TAG,"Response: "+response.message());
            }

            RxBus.getBusInstance().send(new ConnectionStatusUpdate(ConnectionStatusUpdate.DISCONNECTED));
            mSocketIndex++;

            if(mSocketIndex > mNodeUrls.size() * 3){
                Log.e(TAG,"Giving up on connections");
                stopSelf();
            }else{
                connect();
            }
        }
    };
}
