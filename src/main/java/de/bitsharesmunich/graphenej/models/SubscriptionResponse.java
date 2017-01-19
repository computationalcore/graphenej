package de.bitsharesmunich.graphenej.models;

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

import de.bitsharesmunich.graphenej.GrapheneObject;
import de.bitsharesmunich.graphenej.ObjectType;

/**
 * Created by nelson on 1/12/17.
 */
public class SubscriptionResponse {
    private static final String TAG = "SubscriptionResponse";
    public static final String KEY_ID = "id";
    public static final String KEY_METHOD = "method";
    public static final String KEY_PARAMS = "params";

    public String method;
    public List<Serializable> params;

    public static class SubscriptionResponseDeserializer implements JsonDeserializer<SubscriptionResponse> {

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
                    }
                }else{
                    secondArgument.add(object.getAsString());
                }
            }
            return response;
        }
    }
}
