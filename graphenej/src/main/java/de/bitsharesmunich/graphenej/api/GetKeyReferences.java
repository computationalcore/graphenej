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

import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 11/15/16.
 */
public class GetKeyReferences extends BaseGrapheneHandler {

    private List<Address> addresses;

    private boolean mOneTime;

    public GetKeyReferences(Address address, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        addresses = new ArrayList<>();
        addresses.add(address);
        this.mOneTime = oneTime;

    }

    public GetKeyReferences(List<Address> addresses, boolean oneTime, WitnessResponseListener listener) {
        super(listener);
        this.addresses = addresses;
        this.mListener = listener;
        this.mOneTime = oneTime;
    }

    public GetKeyReferences(Address address, WitnessResponseListener listener){
        this(address, true, listener);
    }

    public GetKeyReferences(List<Address> addresses, WitnessResponseListener listener) {
        this(addresses, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> inner = new ArrayList();
        for(Address addr : addresses){
            inner.add(addr.toString());
        }
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(inner);
        ApiCall getAccountByAddress = new ApiCall(0, RPC.CALL_GET_KEY_REFERENCES, params, RPC.VERSION, 1);
        websocket.sendText(getAccountByAddress.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        GsonBuilder builder = new GsonBuilder();

        Type GetAccountByAddressResponse = new TypeToken<WitnessResponse<List<List<UserAccount>>>>(){}.getType();
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
        WitnessResponse<List<List<UserAccount>>> witnessResponse = builder.create().fromJson(response, GetAccountByAddressResponse);
        if (witnessResponse.error != null) {
            this.mListener.onError(witnessResponse.error);
        } else {
            this.mListener.onSuccess(witnessResponse);
        }
        if(mOneTime){
            websocket.disconnect();
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}
