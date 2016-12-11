package de.bitsharesmunich.graphenej.models;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to build a Graphene websocket API call.
 * @see <a href="http://docs.bitshares.org/api/websocket.html">Websocket Calls & Notifications</a>
 */
public class ApiCall implements JsonSerializable {
    public static final String KEY_SEQUENCE_ID = "id";
    public static final String KEY_METHOD = "method";
    public static final String KEY_PARAMS = "params";
    public static final String KEY_JSON_RPC = "jsonrpc";

    public String method;
    public String methodToCall;
    public String jsonrpc;
    public List<Serializable> params;
    public int apiId;
    public int sequenceId;

    public ApiCall(int apiId, String methodToCall, List<Serializable> params, String jsonrpc, int sequenceId){
        this.apiId = apiId;
        this.method = "call";
        this.methodToCall = methodToCall;
        this.jsonrpc = jsonrpc;
        this.params = params;
        this.sequenceId = sequenceId;
    }

    public ApiCall(int apiId, String method, String methodToCall, List<Serializable> params, String jsonrpc, int sequenceId){
        this.apiId = apiId;
        this.method = method;
        this.methodToCall = methodToCall;
        this.jsonrpc = jsonrpc;
        this.params = params;
        this.sequenceId = sequenceId;
    }

    @Override
    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ApiCall.class, new ApiCallSerializer());
        return gsonBuilder.create().toJson(this);
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_SEQUENCE_ID, this.sequenceId);
        obj.addProperty(KEY_METHOD, this.method);
        JsonArray paramsArray = new JsonArray();
        paramsArray.add(this.apiId);
        paramsArray.add(this.methodToCall);
        JsonArray methodParams = new JsonArray();

        for(int i = 0; i < this.params.size(); i++){
            if(this.params.get(i) instanceof JsonSerializable) {
                // Sometimes the parameters are objects
                methodParams.add(((JsonSerializable) this.params.get(i)).toJsonObject());
            }else if (Number.class.isInstance(this.params.get(i))){
                // Other times they are numbers
                methodParams.add( (Number) this.params.get(i));
            }else if(this.params.get(i) instanceof String || this.params.get(i) == null){
                // Other times they are plain strings
                methodParams.add((String) this.params.get(i));
            }else if(this.params.get(i) instanceof ArrayList){
                // Other times it might be an array
                JsonArray array = new JsonArray();
                ArrayList<Serializable> listArgument = (ArrayList<Serializable>) this.params.get(i);
                for(int l = 0; l < listArgument.size(); l++){
                    Serializable element = listArgument.get(l);
                    if(element instanceof JsonSerializable)
                        array.add(((JsonSerializable) element).toJsonObject());
                    else if(element instanceof String){
                        array.add((String) element);
                    }
                }
                methodParams.add(array);
            }else{
                System.out.println("Skipping parameter of type: "+this.params.get(i).getClass());
            }
        }
        paramsArray.add(methodParams);
        obj.add(KEY_PARAMS, paramsArray);
        obj.addProperty(KEY_JSON_RPC, this.jsonrpc);
        return obj;
    }

    class ApiCallSerializer implements JsonSerializer<ApiCall> {

        @Override
        public JsonElement serialize(ApiCall apiCall, Type type, JsonSerializationContext jsonSerializationContext) {
            return toJsonObject();
        }
    }
}
