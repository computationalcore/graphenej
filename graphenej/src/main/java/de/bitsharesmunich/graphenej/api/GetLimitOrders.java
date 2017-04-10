package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.LimitOrder;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 11/15/16.
 */
public class GetLimitOrders extends BaseGrapheneHandler {

    private String a;
    private String b;
    private int limit;
    private WitnessResponseListener mListener;

    public GetLimitOrders(String a, String b, int limit, WitnessResponseListener mListener) {
        super(mListener);
        this.a = a;
        this.b = b;
        this.limit = limit;
        this.mListener = mListener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.a);
        accountParams.add(this.b);
        accountParams.add(this.limit);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_LIMIT_ORDERS, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println("<<< "+frame.getPayloadText());
        try {
            String response = frame.getPayloadText();
            GsonBuilder builder = new GsonBuilder();

            Type GetLimitOrdersResponse = new TypeToken<WitnessResponse<List<LimitOrder>>>() {}.getType();
            builder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
            builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
            builder.registerTypeAdapter(LimitOrder.class, new LimitOrder.LimitOrderDeserializer());
            WitnessResponse<List<LimitOrder>> witnessResponse = builder.create().fromJson(response, GetLimitOrdersResponse);
            if (witnessResponse.error != null) {
                this.mListener.onError(witnessResponse.error);
            } else {
                this.mListener.onSuccess(witnessResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">>> "+frame.getPayloadText());
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}
