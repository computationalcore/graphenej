package cy.agorise.graphenej.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.ObjectType;
import cy.agorise.graphenej.OperationType;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.interfaces.SubscriptionListener;

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
 */
public class SubscriptionResponse {
    public static final String KEY_ID = "id";
    public static final String KEY_METHOD = "method";
    public static final String KEY_PARAMS = "params";

    public String method;
    public List<Serializable> params;

    /**
     * Inner static class used to parse and deserialize subscription responses in a partial way,
     * depending on the amount of SubscriptionListeners we might have registered.
     *
     * The rationale behind these architecture is to avoid wasting computational resources parsing unneeded
     * objects that might come once the are subscribed to the witness notifications.
     */
    public static class SubscriptionResponseDeserializer implements JsonDeserializer<SubscriptionResponse> {
        /**
         * Map of ObjectType to Integer used to keep track of the current amount of listener per type
         */
        private HashMap<ObjectType, Integer> listenerTypeCount;

        /**
         * List of listeners
         */
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

        /**
         * Adds a subscription listener to the list.
         * @param subscriptionListener: Class implementing the {@see SubscriptionListener} interface
         *                            to be added to the list.
         */
        public void addSubscriptionListener(SubscriptionListener subscriptionListener){
            int currentCount = 0;
            if(listenerTypeCount.containsKey(subscriptionListener.getInterestObjectType())){
                currentCount = listenerTypeCount.get(subscriptionListener.getInterestObjectType());
            }
            this.listenerTypeCount.put(subscriptionListener.getInterestObjectType(), currentCount + 1);
            this.mListeners.add(subscriptionListener);
        }

        /**
         * Retrieves the full list of SubscriptionListeners registered.
         * @return
         */
        public List<SubscriptionListener> getSubscriptionListeners(){
            return this.mListeners;
        }

        /**
         * Removes a subscription listener to the list.
         * @param subscriptionListener: Class implementing the {@see SubscriptionListener} interface
         *                            to be removed from the list.
         */
        public void removeSubscriptionListener(SubscriptionListener subscriptionListener){
            if(listenerTypeCount.containsKey(subscriptionListener.getInterestObjectType())){
                int currentCount = listenerTypeCount.get(subscriptionListener.getInterestObjectType());
                if(currentCount > 0){
                    this.listenerTypeCount.put(subscriptionListener.getInterestObjectType(), currentCount - 1);
                    this.mListeners.remove(subscriptionListener);
                }else{
                    System.out.println("Trying to remove subscription listener, but none is registered!");
                }
            }
        }

        /**
         * Removes all registered subscription listeners
         */
        public void clearAllSubscriptionListeners(){
            this.mListeners.clear();
            this.listenerTypeCount.clear();
        }

        @Override
        public SubscriptionResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SubscriptionResponse response = new SubscriptionResponse();
            JsonObject responseObject = json.getAsJsonObject();
            if(!responseObject.has(KEY_METHOD)){
                return response;
            }
            response.method = responseObject.get(KEY_METHOD).getAsString();

            JsonArray paramsArray = responseObject.get(KEY_PARAMS).getAsJsonArray();
            response.params = new ArrayList<>();
            response.params.add(paramsArray.get(0).getAsInt());
            ArrayList<Serializable> secondArgument = new ArrayList<>();
            response.params.add(secondArgument);

            // Hash map used to record the type of objects present in this subscription message
            // and only alert listeners that might be interested
            HashMap<ObjectType, Boolean> objectMap = new HashMap<>();

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
                            objectMap.put(ObjectType.ACCOUNT_BALANCE_OBJECT, true);
                            secondArgument.add(balanceObject);
                        }else if(grapheneObject.getObjectType() == ObjectType.DYNAMIC_GLOBAL_PROPERTY_OBJECT){
                            DynamicGlobalProperties dynamicGlobalProperties = context.deserialize(object, DynamicGlobalProperties.class);
                            objectMap.put(ObjectType.DYNAMIC_GLOBAL_PROPERTY_OBJECT, true);
                            secondArgument.add(dynamicGlobalProperties);
                        }else if(grapheneObject.getObjectType() == ObjectType.TRANSACTION_OBJECT){
                            BroadcastedTransaction broadcastedTransaction = new BroadcastedTransaction(grapheneObject.getObjectId());
                            broadcastedTransaction.setTransaction((Transaction) context.deserialize(jsonObject.get(BroadcastedTransaction.KEY_TRX), Transaction.class));
                            broadcastedTransaction.setTransactionId(jsonObject.get(BroadcastedTransaction.KEY_TRX_ID).getAsString());
                            objectMap.put(ObjectType.TRANSACTION_OBJECT, true);
                            secondArgument.add(broadcastedTransaction);
                        }else if(grapheneObject.getObjectType() == ObjectType.OPERATION_HISTORY_OBJECT){
                            if(jsonObject.get(OperationHistory.KEY_OP).getAsJsonArray().get(0).getAsLong() == OperationType.TRANSFER_OPERATION.ordinal()){
                                OperationHistory operationHistory = context.deserialize(jsonObject, OperationHistory.class);
                                objectMap.put(ObjectType.OPERATION_HISTORY_OBJECT, true);
                                secondArgument.add(operationHistory);
                            }else{
                                //TODO: Add support for other operations
                            }
                        }else{
                            //TODO: Add support for other types of objects
                        }
                    }
                }else{
                    secondArgument.add(object.getAsString());
                }
            }
            for(SubscriptionListener listener : mListeners){
                // Only notify the listener if there is an object of interest in
                // this notification
                if(objectMap.containsKey(listener.getInterestObjectType())){
                    listener.onSubscriptionUpdate(response);
                }
            }
            return response;
        }
    }
}
