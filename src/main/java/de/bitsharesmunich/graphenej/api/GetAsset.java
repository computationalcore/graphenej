package com.luminiasoft.bitshares.ws;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.Asset;
import com.luminiasoft.bitshares.RPC;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
import com.luminiasoft.bitshares.models.AccountProperties;
import com.luminiasoft.bitshares.models.ApiCall;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nelson on 11/15/16.
 */
public class GetAsset extends WebSocketAdapter {

    private String assetName;
    private WitnessResponseListener mListener;

    public GetAsset(String assetName, WitnessResponseListener listener){
        this.assetName = assetName;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        ArrayList<Serializable> assetList = new ArrayList();
        assetList.add(new JsonSerializable() {
            @Override
            public String toJsonString() {
                return assetName;
            }

            @Override
            public JsonElement toJsonObject() {
                return new JsonParser().parse(assetName);
            }
        });
        accountParams.add(assetList);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_ASSET, accountParams, "2.0", 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Gson gson = new Gson();

        Type getAssetResponse = new TypeToken<WitnessResponse<ArrayList<Asset>>>(){}.getType();
        WitnessResponse<ArrayList<Asset>> witnessResponse = gson.fromJson(response, getAssetResponse);

        if(witnessResponse.error != null){
            this.mListener.onError(witnessResponse.error);
        }else{
            this.mListener.onSuccess(witnessResponse);
        }

        websocket.disconnect();
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
