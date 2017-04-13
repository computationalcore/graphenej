package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.HistoricalTransfer;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.operations.TransferOperation;

/**
 * Class used to encapsulate the communication sequence used to retrieve the transaction history of
 * a given user.
 */
public class GetRelativeAccountHistory extends BaseGrapheneHandler {
    // Sequence of message ids
    private final static int LOGIN_ID = 1;
    private final static int GET_HISTORY_ID = 2;
    private final static int GET_HISTORY_DATA = 3;

    // Default value constants
    public static final int DEFAULT_STOP = 0;
    public static final int DEFAULT_START = 0;
    public static final int MAX_LIMIT = 100;

    // API call parameters
    private UserAccount mUserAccount;
    private int stop;
    private int limit;
    private int start;
    private WitnessResponseListener mListener;
    private WebSocket mWebsocket;

    private int currentId = 1;
    private int apiId = -1;

    /**
     * Constructor that takes all possible parameters.
     * @param userAccount The user account to be queried
     * @param stop Sequence number of earliest operation
     * @param limit Maximum number of operations to retrieve (must not exceed 100)
     * @param start Sequence number of the most recent operation to retrieve
     * @param listener Listener to be notified with the result of this query
     */
    public GetRelativeAccountHistory(UserAccount userAccount, int stop, int limit, int start, WitnessResponseListener listener){
        super(listener);
        if(limit > MAX_LIMIT) limit = MAX_LIMIT;
        this.mUserAccount = userAccount;
        this.stop = stop;
        this.limit = limit;
        this.start = start;
        this.mListener = listener;
    }

    /**
     * Constructor that uses the default values, and sets the limit to its maximum possible value.
     * @param userAccount The user account to be queried
     * @param listener Listener to be notified with the result of this query
     */
    public GetRelativeAccountHistory(UserAccount userAccount, WitnessResponseListener listener){
        super(listener);
        this.mUserAccount = userAccount;
        this.stop = DEFAULT_STOP;
        this.limit = MAX_LIMIT;
        this.start = DEFAULT_START;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        mWebsocket = websocket;
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        System.out.println("<<< "+response);
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            websocket.disconnect();
        }else{
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if(baseResponse.id == LOGIN_ID){
                ApiCall getRelativeAccountHistoryId = new ApiCall(1, RPC.CALL_HISTORY, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(getRelativeAccountHistoryId.toJsonString());
            }else if(baseResponse.id == GET_HISTORY_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                apiId = witnessResponse.result.intValue();

                sendRelativeAccountHistoryRequest();
            }else if(baseResponse.id >= GET_HISTORY_DATA){
                Type RelativeAccountHistoryResponse = new TypeToken<WitnessResponse<List<HistoricalTransfer>>>(){}.getType();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer());
                gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
                WitnessResponse<List<HistoricalTransfer>> transfersResponse = gsonBuilder.create().fromJson(response, RelativeAccountHistoryResponse);
                mListener.onSuccess(transfersResponse);
            }
        }
    }

    /**
     * Sends the actual get_relative_account_history request.
     */
    private void sendRelativeAccountHistoryRequest(){
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(mUserAccount.toJsonString());
        params.add(this.stop);
        params.add(this.limit);
        params.add(this.start);

        ApiCall getRelativeAccountHistoryCall = new ApiCall(apiId, RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY, params, RPC.VERSION, currentId);
        mWebsocket.sendText(getRelativeAccountHistoryCall.toJsonString());
    }

    /**
     * Updates the arguments and makes a new call to the get_relative_account_history API
     * @param stop Sequence number of earliest operation
     * @param limit Maximum number of operations to retrieve (must not exceed 100)
     * @param start Sequence number of the most recent operation to retrieve
     */
    public void retry(int stop, int limit, int start){
        this.stop = stop;
        this.limit = limit;
        this.start = start;
        sendRelativeAccountHistoryRequest();
    }

    /**
     * Disconnects the websocket
     */
    public void disconnect(){
        if(mWebsocket != null && mWebsocket.isOpen()){
            mWebsocket.disconnect();
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}
