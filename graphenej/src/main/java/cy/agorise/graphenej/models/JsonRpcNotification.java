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
import java.util.List;

import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.ObjectType;
import cy.agorise.graphenej.OperationType;
import cy.agorise.graphenej.Transaction;

/**
 * Class that represents a generic subscription notification.
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
 */
public class JsonRpcNotification {
    public static final String KEY_METHOD = "method";
    public static final String KEY_PARAMS = "params";

    public String method;
    public List<Serializable> params;

    /**
     * Inner static class used to parse and deserialize subscription notifications.
     */
    public static class JsonRpcNotificationDeserializer implements JsonDeserializer<JsonRpcNotification> {

        @Override
        public JsonRpcNotification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonRpcNotification notification = new JsonRpcNotification();
            JsonObject responseObject = json.getAsJsonObject();
            if(!responseObject.has(KEY_METHOD)){
                return notification;
            }
            notification.method = responseObject.get(KEY_METHOD).getAsString();

            JsonArray paramsArray = responseObject.get(KEY_PARAMS).getAsJsonArray();
            notification.params = new ArrayList<>();
            notification.params.add(paramsArray.get(0).getAsInt());
            ArrayList<Serializable> secondArgument = new ArrayList<>();
            notification.params.add(secondArgument);

            JsonArray subArray = paramsArray.get(1).getAsJsonArray().get(0).getAsJsonArray();
            for(JsonElement object : subArray){
                if(object.isJsonObject()){
                    GrapheneObject grapheneObject = new GrapheneObject(object.getAsJsonObject().get(GrapheneObject.KEY_ID).getAsString());

                    JsonObject jsonObject = object.getAsJsonObject();
                    if(grapheneObject.getObjectType() == ObjectType.ACCOUNT_BALANCE_OBJECT){
                        AccountBalanceUpdate balanceObject = new AccountBalanceUpdate(grapheneObject.getObjectId());
                        balanceObject.owner = jsonObject.get(AccountBalanceUpdate.KEY_OWNER).getAsString();
                        balanceObject.asset_type = jsonObject.get(AccountBalanceUpdate.KEY_ASSET_TYPE).getAsString();
                        balanceObject.balance = jsonObject.get(AccountBalanceUpdate.KEY_BALANCE).getAsLong();
                        secondArgument.add(balanceObject);
                    }else if(grapheneObject.getObjectType() == ObjectType.DYNAMIC_GLOBAL_PROPERTY_OBJECT){
                        DynamicGlobalProperties dynamicGlobalProperties = context.deserialize(object, DynamicGlobalProperties.class);
                        secondArgument.add(dynamicGlobalProperties);
                    }else if(grapheneObject.getObjectType() == ObjectType.TRANSACTION_OBJECT){
                        BroadcastedTransaction broadcastedTransaction = new BroadcastedTransaction(grapheneObject.getObjectId());
                        broadcastedTransaction.setTransaction((Transaction) context.deserialize(jsonObject.get(BroadcastedTransaction.KEY_TRX), Transaction.class));
                        broadcastedTransaction.setTransactionId(jsonObject.get(BroadcastedTransaction.KEY_TRX_ID).getAsString());
                        secondArgument.add(broadcastedTransaction);
                    }else if(grapheneObject.getObjectType() == ObjectType.OPERATION_HISTORY_OBJECT){
                        if(jsonObject.get(OperationHistory.KEY_OP).getAsJsonArray().get(0).getAsLong() == OperationType.TRANSFER_OPERATION.ordinal()){
                            OperationHistory operationHistory = context.deserialize(jsonObject, OperationHistory.class);
                            secondArgument.add(operationHistory);
                        }else{
                            //TODO: Add support for other operations
                        }
                    }else{
                        //TODO: Add support for other types of objects
                    }
                }else{
                    secondArgument.add(object.getAsString());
                }
            }
            return notification;
        }
    }
}
