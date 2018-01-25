package cy.agorise.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Class that implements get_all_asset_holders request handler.
 *
 *  Get a list of all system assets with holders count.
 *
 *  The response returns the list of all assets with holders count.
 *
 *  @see <a href="https://goo.gl/AgTSLU">get_all_asset_holders API doc (source code ref.)</a>
 */
public class GetAllAssetHolders extends BaseGrapheneHandler {
    private final static int LOGIN_ID = 1;
    private final static int GET_ASSET_API_ID = 2;
    private final static int GET_ALL_ASSET_HOLDERS_COUNT = 3;

    private int currentId = 1;
    private int assetApiId = -1;

    private boolean mOneTime;

    /**
     * Default Constructor
     *
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAllAssetHolders(boolean oneTime, WitnessResponseListener listener) {
        super(listener);
        this.mOneTime = oneTime;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param listener A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public GetAllAssetHolders(WitnessResponseListener listener) { this(true, listener);}

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            if(mOneTime){
                websocket.disconnect();
            }
        }else {
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if (baseResponse.id == LOGIN_ID) {
                ApiCall networkApiIdCall = new ApiCall(1, RPC.CALL_ASSET, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(networkApiIdCall.toJsonString());
            }else if(baseResponse.id == GET_ASSET_API_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                assetApiId = witnessResponse.result;

                ApiCall apiCall = new ApiCall(assetApiId, RPC.CALL_GET_ALL_ASSET_HOLDERS, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(apiCall.toJsonString());
            } else if (baseResponse.id == GET_ALL_ASSET_HOLDERS_COUNT) {
                Type AssetTokenHolders = new TypeToken<WitnessResponse<List<AssetHolderCount>>>(){}.getType();
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(AssetHolderCount.class, new AssetHolderCount.HoldersCountDeserializer());
                WitnessResponse<List<AssetHolderCount>> witnessResponse = builder.create().fromJson(response, AssetTokenHolders);
                mListener.onSuccess(witnessResponse);
                if(mOneTime){
                    websocket.disconnect();
                }
            }else{
                System.out.println("current id: "+currentId);
            }
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">>> "+frame.getPayloadText());
        }
    }
}
