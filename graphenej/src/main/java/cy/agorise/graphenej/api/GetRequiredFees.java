package cy.agorise.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.BaseOperation;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Class that implements get_required_fees request handler.
 *
 *  For each operation calculate the required fee in the specified asset type.
 *
 *  @see <a href="https://goo.gl/MB4TXq">get_required_fees API doc</a>
 */
public class GetRequiredFees extends BaseGrapheneHandler {

    private WitnessResponseListener mListener;
    private List<BaseOperation> operations;
    private Asset asset;

    private boolean mOneTime;

    /**
     * Default Constructor
     *
     * @param operations    list of operations that fee should be calculated
     * @param asset         specify the asset of the operations
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetRequiredFees(List<BaseOperation> operations, Asset asset, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.operations = operations;
        this.asset = asset;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param operations    list of operations that fee should be calculated
     * @param asset         specify the asset of the operations
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetRequiredFees(List<BaseOperation> operations, Asset asset, WitnessResponseListener listener){
        this(operations, asset, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add((Serializable) this.operations);
        accountParams.add(this.asset.getObjectId());
        ApiCall getRequiredFees = new ApiCall(0, RPC.CALL_GET_REQUIRED_FEES, accountParams, RPC.VERSION, 1);
        websocket.sendText(getRequiredFees.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Gson gson = new Gson();

        Type GetRequiredFeesResponse = new TypeToken<WitnessResponse<List<AssetAmount>>>(){}.getType();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        WitnessResponse<List<AssetAmount>> witnessResponse = gsonBuilder.create().fromJson(response, GetRequiredFeesResponse);

        if(witnessResponse.error != null){
            mListener.onError(witnessResponse.error);
        }else{
            mListener.onSuccess(witnessResponse);
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        if(mOneTime){
            websocket.disconnect();
        }
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        if(mOneTime){
            websocket.disconnect();
        }
    }
}
