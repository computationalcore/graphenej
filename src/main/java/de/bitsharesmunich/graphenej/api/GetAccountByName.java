package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.bitsharesmunich.graphenej.AccountOptions;
import de.bitsharesmunich.graphenej.Authority;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.AccountProperties;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
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
public class GetAccountByName extends WebSocketAdapter {

    private String accountName;
    private WitnessResponseListener mListener;

    public GetAccountByName(String accountName, WitnessResponseListener listener){
        this.accountName = accountName;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_ACCOUNT_BY_NAME, accountParams, "2.0", 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        GsonBuilder builder = new GsonBuilder();

        Type GetAccountByNameResponse = new TypeToken<WitnessResponse<AccountProperties>>(){}.getType();
        builder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
        builder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
        WitnessResponse<AccountProperties> witnessResponse = builder.create().fromJson(response, GetAccountByNameResponse);

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
