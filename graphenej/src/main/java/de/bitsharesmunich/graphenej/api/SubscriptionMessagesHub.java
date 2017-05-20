package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.ObjectType;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.Transaction;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.SubscriptionHub;
import de.bitsharesmunich.graphenej.interfaces.SubscriptionListener;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.DynamicGlobalProperties;
import de.bitsharesmunich.graphenej.models.SubscriptionResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.operations.LimitOrderCreateOperation;
import de.bitsharesmunich.graphenej.operations.TransferOperation;

/**
 * A websocket adapter prepared to be used as a basic dispatch hub for subscription messages.
 *
 * Created by nelson on 1/26/17.
 */
public class SubscriptionMessagesHub extends BaseGrapheneHandler implements SubscriptionHub {
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
    private boolean clearFilter;
    private List<ObjectType> objectTypes;
    private int currentId;
    private int databaseApiId = -1;
    private int subscriptionCounter = 0;

    /**
     * Id used to separate requests regarding the subscriptions
     */
    private final int SUBSCRIPTION_ID = 10;

    /**
     * Constructor used to create a subscription message hub that will call the set_subscribe_callback
     * API with the clear_filter parameter set to false, meaning that it will only receive automatic updates
     * from objects we register.
     *
     * A list of ObjectTypes must be provided, otherwise we won't get any update.
     *
     * @param user: User name, in case the node to which we're going to connect to requires authentication
     * @param password: Password, same as above
     * @param objectTypes: List of objects of interest
     * @param errorListener: Callback that will be fired in case there is an error.
     */
    public SubscriptionMessagesHub(String user, String password, List<ObjectType> objectTypes, WitnessResponseListener errorListener){
        super(errorListener);
        this.objectTypes = objectTypes;
        this.user = user;
        this.password = password;
        this.clearFilter = true;
        this.mSubscriptionDeserializer = new SubscriptionResponse.SubscriptionResponseDeserializer();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SubscriptionResponse.class, mSubscriptionDeserializer);
        builder.registerTypeAdapter(Transaction.class, new Transaction.TransactionDeserializer());
        builder.registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer());
        builder.registerTypeAdapter(LimitOrderCreateOperation.class, new LimitOrderCreateOperation.LimitOrderCreateDeserializer());
        builder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
        builder.registerTypeAdapter(DynamicGlobalProperties.class, new DynamicGlobalProperties.DynamicGlobalPropertiesDeserializer());
        this.gson = builder.create();
    }

    /**
     * Constructor used to create a subscription message hub that will call the set_subscribe_callback
     * API with the clear_filter parameter set to true, meaning that it will receive automatic updates
     * on all network events.
     *
     * @param user: User name, in case the node to which we're going to connect to requires authentication
     * @param password: Password, same as above
     * @param errorListener: Callback that will be fired in case there is an error.
     */
    public SubscriptionMessagesHub(String user, String password, WitnessResponseListener errorListener){
        this(user, password, new ArrayList<ObjectType>(), errorListener);
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
        currentId = LOGIN_ID;
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
            currentId++;
        }else if(currentId == GET_DATABASE_ID){
            Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
            WitnessResponse<Integer> witnessResponse = gson.fromJson(message, ApiIdResponse);
            databaseApiId = witnessResponse.result;

            ArrayList<Serializable> subscriptionParams = new ArrayList<>();
            subscriptionParams.add(String.format("%d", SUBCRIPTION_NOTIFICATION));
            subscriptionParams.add(clearFilter);
            ApiCall getDatabaseId = new ApiCall(databaseApiId, RPC.CALL_SET_SUBSCRIBE_CALLBACK, subscriptionParams, RPC.VERSION, SUBCRIPTION_REQUEST);
            websocket.sendText(getDatabaseId.toJsonString());
            currentId++;
        } else if(currentId == SUBCRIPTION_REQUEST){
            if(objectTypes != null && objectTypes.size() > 0 && subscriptionCounter < objectTypes.size()){
                ArrayList<Serializable> objectOfInterest = new ArrayList<>();
                objectOfInterest.add(objectTypes.get(subscriptionCounter).getGenericObjectId());
                ArrayList<Serializable> payload = new ArrayList<>();
                payload.add(objectOfInterest);
                ApiCall subscribe = new ApiCall(databaseApiId, RPC.GET_OBJECTS, payload, RPC.VERSION, SUBSCRIPTION_ID);
                websocket.sendText(subscribe.toJsonString());
                subscriptionCounter++;
            }else{
                gson.fromJson(message, SubscriptionResponse.class);
            }
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println(">> "+frame.getPayloadText());
    }

    public void reset(){
        currentId = 0;
        databaseApiId = -1;
        subscriptionCounter = 0;
    }
}
