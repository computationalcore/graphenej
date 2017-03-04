package de.bitsharesmunich.graphenej.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

import de.bitsharesmunich.graphenej.*;
import de.bitsharesmunich.graphenej.interfaces.SubscriptionListener;

/**
 * Class that represents a generic subscription response.
 * The template for every subscription response is the following:
 *
 *  {
 *    "method": "notice"
 *    "params": [
 *      SUBSCRIPTION_ID,
 *        [[
 *          { "id": "2.1.0", ...  },
 *          { "id": ...  },
 *          { "id": ...  },
 *          { "id": ...  }
 *        ]]
 *    ],
 *  }
 *
 * As of 1/2017, the witness API returns all sort of events, not just the ones we're interested in once we
 * make a call to the 'set_subscribe_callback', regardless of whether the 'clear_filter' parameter is set to
 * true or false.
 *
 * To minimize CPU usage, we introduce a scheme of selective parsing, implemented by the static inner class
 * SubscriptionResponseDeserializer.
 *
 * Created by nelson on 1/12/17.
 */
public class SubscriptionResponse {
    private static final String TAG = "SubscriptionResponse";
    public static final String KEY_ID = "id";
    public static final String KEY_METHOD = "method";
    public static final String KEY_PARAMS = "params";

    public String method;
    public List<Serializable> params;

    /**
     * Deserializer class that is used to parse and deserialize subscription responses in a partial way,
     * depending on the amount of SubscriptionListeners we might have registered.
     *
     * The rationale behind these architecture is to avoid wasting computational resources parsing unneeded
     * objects that might come once the are subscribed to the witness notifications.
     */
    public static class SubscriptionResponseDeserializer implements JsonDeserializer<SubscriptionResponse> {
        private HashMap<ObjectType, Integer> listenerTypeCount;
        private LinkedList<SubscriptionListener> mListeners;

        /**
         * Constructor that will just create a list of SubscriptionListeners and
         * a map of ObjectType to integer in order to keep track of how many listeners
         * to each type of object we have.
         */
        public SubscriptionResponseDeserializer(){
            mListeners = new LinkedList<>();
            listenerTypeCount = new HashMap<>();
        }

        public void addSubscriptionListener(SubscriptionListener subscriptionListener){
            int currentCount = 0;
            if(listenerTypeCount.containsKey(subscriptionListener.getInterestObjectType())){
                currentCount = listenerTypeCount.get(subscriptionListener.getInterestObjectType());
            }
            this.listenerTypeCount.put(subscriptionListener.getInterestObjectType(), currentCount + 1);
            this.mListeners.add(subscriptionListener);
        }

        public List<SubscriptionListener> getSubscriptionListeners(){
            return this.mListeners;
        }

        public void removeSubscriptionListener(SubscriptionListener subscriptionListener){
            int currentCount = listenerTypeCount.get(subscriptionListener.getInterestObjectType());
            if(currentCount != 0){
                this.listenerTypeCount.put(subscriptionListener.getInterestObjectType(), currentCount);
            }else{
                System.out.println("Trying to remove subscription listener, but none is registered!");
            }
            this.mListeners.remove(subscriptionListener);
        }

        @Override
        public SubscriptionResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SubscriptionResponse response = new SubscriptionResponse();
            JsonObject responseObject = json.getAsJsonObject();
            response.method = responseObject.get(KEY_METHOD).getAsString();

            JsonArray paramsArray = responseObject.get(KEY_PARAMS).getAsJsonArray();
            response.params = new ArrayList<>();
            response.params.add(paramsArray.get(0).getAsInt());
            ArrayList<Serializable> secondArgument = new ArrayList<>();
            response.params.add(secondArgument);

            JsonArray subArray = paramsArray.get(1).getAsJsonArray().get(0).getAsJsonArray();
            for(JsonElement object : subArray){
                if(object.isJsonObject()){

                    GrapheneObject grapheneObject = new GrapheneObject(object.getAsJsonObject().get(KEY_ID).getAsString());
                    int listenerTypeCount = 0;
                    if(this.listenerTypeCount.containsKey(grapheneObject.getObjectType())){
                        listenerTypeCount = this.listenerTypeCount.get(grapheneObject.getObjectType());
                    }
                    /*
                     * Here's where we apply the selective deserialization logic, meaning we only completely deserialize
                     * an object contained in a notification if there is at least one registered listener interested in
                     * objects of that type.
                     */
                    if(listenerTypeCount > 0){
                        JsonObject jsonObject = object.getAsJsonObject();
                        if(grapheneObject.getObjectType() == ObjectType.ACCOUNT_BALANCE_OBJECT){
                            AccountBalanceUpdate balanceObject = new AccountBalanceUpdate(grapheneObject.getObjectId());
                            balanceObject.owner = jsonObject.get(AccountBalanceUpdate.KEY_OWNER).getAsString();
                            balanceObject.asset_type = jsonObject.get(AccountBalanceUpdate.KEY_ASSET_TYPE).getAsString();
                            balanceObject.balance = jsonObject.get(AccountBalanceUpdate.KEY_BALANCE).getAsLong();
                            secondArgument.add(balanceObject);
                        }else if(grapheneObject.getObjectType() == ObjectType.DYNAMIC_GLOBAL_PROPERTY_OBJECT){
                            DynamicGlobalProperties dynamicGlobal = new DynamicGlobalProperties(grapheneObject.getObjectId());
                            dynamicGlobal.head_block_number = jsonObject.get(DynamicGlobalProperties.KEY_HEAD_BLOCK_NUMBER).getAsLong();
                            dynamicGlobal.head_block_id = jsonObject.get(DynamicGlobalProperties.KEY_HEAD_BLOCK_ID).getAsString();
                            dynamicGlobal.time = jsonObject.get(DynamicGlobalProperties.KEY_TIME).getAsString();
                            //TODO: Deserialize all other attributes
                            secondArgument.add(dynamicGlobal);
                        }else if(grapheneObject.getObjectType() == ObjectType.TRANSACTION_OBJECT){
                            BroadcastedTransaction broadcastedTransaction = new BroadcastedTransaction(grapheneObject.getObjectId());
                            broadcastedTransaction.setTransaction((Transaction) context.deserialize(jsonObject.get(BroadcastedTransaction.KEY_TRX), Transaction.class));
                            broadcastedTransaction.setTransactionId(jsonObject.get(BroadcastedTransaction.KEY_TRX_ID).getAsString());
                            secondArgument.add(broadcastedTransaction);
                        }else{
                            //TODO: Add support for other types of objects
                        }
                    }
                }else{
                    secondArgument.add(object.getAsString());
                }
            }
            for(SubscriptionListener listener : mListeners){
                listener.onSubscriptionUpdate(response);
            }
            return response;
        }
    }
}
