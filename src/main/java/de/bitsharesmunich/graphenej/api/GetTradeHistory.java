package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.bitsharesmunich.graphenej.MarketTrade;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author henry
 */
public class GetTradeHistory extends WebSocketAdapter {

    private String a;
    private String b;
    private String toTime;
    private String fromTime;
    private int limit;
    private WitnessResponseListener mListener;

    public GetTradeHistory(String a, String b, String toTime, String fromTime,int limit, WitnessResponseListener mListener) {
        this.a = a;
        this.b = b;
        this.toTime = toTime;
        this.fromTime = fromTime;
        this.limit = limit;
        this.mListener = mListener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.a);
        accountParams.add(this.b);
        accountParams.add(this.toTime);
        accountParams.add(this.fromTime);
        accountParams.add(this.limit);

        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_TRADE_HISTORY, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if (frame.isTextFrame()) {
            System.out.println("<<< " + frame.getPayloadText());
        }
        try {
            String response = frame.getPayloadText();
            GsonBuilder builder = new GsonBuilder();

            Type GetTradeHistoryResponse = new TypeToken<WitnessResponse<List<MarketTrade>>>() {
            }.getType();
            WitnessResponse<List<MarketTrade>> witnessResponse = builder.create().fromJson(response, GetTradeHistoryResponse);
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
        if (frame.isTextFrame()) {
            System.out.println(">>> " + frame.getPayloadText());
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
