package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.BucketObject;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 12/22/16.
 */
public class GetMarketHistory extends BaseGrapheneHandler {

    // Sequence of message ids
    private final static int LOGIN_ID = 1;
    private final static int GET_HISTORY_ID = 2;
    private final static int GET_HISTORY_DATA = 3;

    // API call parameters
    private Asset base;
    private Asset quote;
    private long bucket;
    private Date start;
    private Date end;
    private WitnessResponseListener mListener;

    private int currentId = 1;
    private int apiId = -1;

    public GetMarketHistory(Asset base, Asset quote, long bucket, Date start, Date end, WitnessResponseListener listener){
        super(listener);
        this.base = base;
        this.quote = quote;
        this.bucket = bucket;
        this.start = start;
        this.end = end;
        this.mListener = listener;
    }

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
            } else if(baseResponse.id == GET_HISTORY_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                apiId = witnessResponse.result.intValue();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

                ArrayList<Serializable> params = new ArrayList<>();
                params.add(this.base.getObjectId());
                params.add(this.quote.getObjectId());
                params.add(this.bucket);
                params.add(dateFormat.format(this.start));
                params.add(dateFormat.format(this.end));

                ApiCall getRelativeAccountHistoryCall = new ApiCall(apiId, RPC.CALL_GET_MARKET_HISTORY, params, RPC.VERSION, currentId);
                websocket.sendText(getRelativeAccountHistoryCall.toJsonString());
            }else if(baseResponse.id == GET_HISTORY_DATA){
                GsonBuilder builder = new GsonBuilder();
                Type MarketHistoryResponse = new TypeToken<WitnessResponse<List<BucketObject>>>(){}.getType();
                builder.registerTypeAdapter(BucketObject.class, new BucketObject.BucketDeserializer());
                WitnessResponse<List<BucketObject>> marketHistoryResponse = builder.create().fromJson(response, MarketHistoryResponse);
                mListener.onSuccess(marketHistoryResponse);
                websocket.disconnect();
            }
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}
