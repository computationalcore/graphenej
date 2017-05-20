package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by henry on 07/12/16.
 */
public class LookupAccounts extends BaseGrapheneHandler {

    public static final int DEFAULT_MAX = 1000;
    private final String accountName;
    private int maxAccounts = DEFAULT_MAX;
    private final WitnessResponseListener mListener;

    public LookupAccounts(String accountName, WitnessResponseListener listener){
        super(listener);
        this.accountName = accountName;
        this.maxAccounts = DEFAULT_MAX;
        this.mListener = listener;
    }

    public LookupAccounts(String accountName, int maxAccounts, WitnessResponseListener listener){
        super(listener);
        this.accountName = accountName;
        this.maxAccounts  = maxAccounts;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        accountParams.add(this.maxAccounts);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_LOOKUP_ACCOUNTS, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();

        Type LookupAccountsResponse = new TypeToken<WitnessResponse<List<UserAccount>>>(){}.getType();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountDeserializer());
        WitnessResponse<List<UserAccount>> witnessResponse = builder.create().fromJson(response, LookupAccountsResponse);
        if(witnessResponse.error != null){
            this.mListener.onError(witnessResponse.error);
        }else{
            this.mListener.onSuccess(witnessResponse);
        }

        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}