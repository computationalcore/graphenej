package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import de.bitsharesmunich.graphenej.AccountOptions;
import de.bitsharesmunich.graphenej.Authority;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

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
        GsonBuilder builder = new GsonBuilder();

        Type GetAccountByAddressResponse = new TypeToken<WitnessResponse<List<AccountProperties>>>() {}.getType();
        builder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
        builder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
        WitnessResponse<List<AccountProperties>> witnessResponse = builder.create().fromJson(response, GetAccountByAddressResponse);

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
