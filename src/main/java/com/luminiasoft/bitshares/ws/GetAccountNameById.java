package com.luminiasoft.bitshares.ws;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
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
 *
 * @author henry
 */
public class GetAccountNameById extends WebSocketAdapter {

    private String accountID;
    private WitnessResponseListener mListener;

    public GetAccountNameById(String accountID, WitnessResponseListener listener) {
        this.accountID = accountID;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList();
        ArrayList<Serializable> paramAddress = new ArrayList();
        paramAddress.add(new JsonSerializable() {
            @Override
            public String toJsonString() {
                return accountID;
            }

            @Override
            public JsonElement toJsonObject() {
                return new JsonParser().parse(accountID);
            }
        });
        accountParams.add(paramAddress);
        ApiCall getAccountByAddress = new ApiCall(0, RPC.CALL_GET_ACCOUNTS, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByAddress.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        Gson gson = new Gson();

        Type GetAccountByAddressResponse = new TypeToken<WitnessResponse<List<AccountProperties>>>() {}.getType();
        WitnessResponse<WitnessResponse<List<AccountProperties>>> witnessResponse = gson.fromJson(response, GetAccountByAddressResponse);

        if (witnessResponse.error != null) {
            this.mListener.onError(witnessResponse.error);
        } else {
            this.mListener.onSuccess(witnessResponse);
        }
        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        System.out.println("onError");
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        System.out.println("handleCallbackError");
        StackTraceElement[] stack = cause.getStackTrace();
        for(StackTraceElement element : stack) {
            System.out.println("> "+element.getClassName()+"."+element.getMethodName()+" : "+element.getLineNumber());
        }
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}
