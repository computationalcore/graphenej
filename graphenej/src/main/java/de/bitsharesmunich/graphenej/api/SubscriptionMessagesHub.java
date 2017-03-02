package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.Transaction;
import de.bitsharesmunich.graphenej.operations.TransferOperation;
import de.bitsharesmunich.graphenej.interfaces.SubscriptionHub;
import de.bitsharesmunich.graphenej.interfaces.SubscriptionListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.SubscriptionResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A websocket adapter prepared to be used as a basic dispatch hub for subscription messages.
 *
 * Created by nelson on 1/26/17.
 */
public class SubscriptionMessagesHub extends WebSocketAdapter implements SubscriptionHub {
    // Sequence of message ids
    private final static int LOGIN_ID = 1;
    private final static int GET_DATABASE_ID = 2;
    private final static int SUBCRIPTION_REQUEST = 3;

    // ID of subscription notifications
    private final static int SUBCRIPTION_NOTIFICATION = 4;

    private SubscriptionResponse.SubscriptionResponseDeserializer mSubscriptionDeserializer;
    private Gson gson;
    private String user;
    private String password;
    private int currentId = LOGIN_ID;
    private int databaseApiId = -1;

    public SubscriptionMessagesHub(String user, String password){
        this.user = user;
        this.password = password;
        this.mSubscriptionDeserializer = new SubscriptionResponse.SubscriptionResponseDeserializer();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SubscriptionResponse.class, mSubscriptionDeserializer);
        builder.registerTypeAdapter(Transaction.class, new Transaction.TransactionDeserializer());
        builder.registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer());
        builder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        this.gson = builder.create();
    }

    @Override
    public void addSubscriptionListener(SubscriptionListener listener){
        this.mSubscriptionDeserializer.addSubscriptionListener(listener);
    }

    @Override
    public void removeSubscriptionListener(SubscriptionListener listener){
        this.mSubscriptionDeserializer.removeSubscriptionListener(listener);
    }

    @Override
    public List<SubscriptionListener> getSubscriptionListeners() {
        return this.mSubscriptionDeserializer.getSubscriptionListeners();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(user);
        loginParams.add(password);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String message = frame.getPayloadText();
        System.out.println("<< "+message);
        if(currentId == LOGIN_ID){
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            ApiCall getDatabaseId = new ApiCall(1, RPC.CALL_DATABASE, emptyParams, RPC.VERSION, currentId);
            websocket.sendText(getDatabaseId.toJsonString());
        }else if(currentId == GET_DATABASE_ID){
            Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
            WitnessResponse<Integer> witnessResponse = gson.fromJson(message, ApiIdResponse);
            databaseApiId = witnessResponse.result;

            ArrayList<Serializable> subscriptionParams = new ArrayList<>();
            subscriptionParams.add(String.format("%d", SUBCRIPTION_NOTIFICATION));
            subscriptionParams.add(false);
            ApiCall getDatabaseId = new ApiCall(databaseApiId, RPC.CALL_SET_SUBSCRIBE_CALLBACK, subscriptionParams, RPC.VERSION, currentId);
            websocket.sendText(getDatabaseId.toJsonString());
        }else if(currentId == SUBCRIPTION_REQUEST){
            // Listeners are called from within the SubscriptionResponseDeserializer, so there's nothing to handle here.
        }else{
            SubscriptionResponse subscriptionResponse = gson.fromJson(message, SubscriptionResponse.class);
        }
        currentId++;
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println(">> "+frame.getPayloadText());
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        super.handleCallbackError(websocket, cause);
    }
}
