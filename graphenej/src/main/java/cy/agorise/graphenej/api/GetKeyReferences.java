package cy.agorise.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *  Class that implements get_key_references request handler.
 *
 *  Retrieve the keys that refer to the address/list of addresses.
 *
 *  The request returns all accounts that refer to the key or account id in their owner or active authorities.
 *
 *  @see <a href="https://goo.gl/np8CYF">get_key_references API doc</a>
 */
public class GetKeyReferences extends BaseGrapheneHandler {

    private List<Address> addresses;

    private boolean mOneTime;

    /**
     * Constructor
     *
     * @param address address to be query
     * @param oneTime boolean value indicating if WebSocket must be closed (true) or not (false)
     *                after the response
     * @param listener A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public GetKeyReferences(Address address, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        addresses = new ArrayList<>();
        addresses.add(address);
        this.mOneTime = oneTime;
    }

    /**
     *
     * @param addresses list of addresses to be query
     * @param oneTime boolean value indicating if WebSocket must be closed (true) or not (false)
     *                after the response
     * @param listener A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public GetKeyReferences(List<Address> addresses, boolean oneTime, WitnessResponseListener listener) {
        super(listener);
        this.addresses = addresses;
        this.mListener = listener;
        this.mOneTime = oneTime;
    }

    /**
     * Using this constructor the websocket connection closes after the response.
     *
     * @param address
     * @param listener A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public GetKeyReferences(Address address, WitnessResponseListener listener){
        this(address, true, listener);
    }

    /**
     * Using this constructor the websocket connection closes after the response.
     *
     * @param addresses
     * @param listener A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
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
