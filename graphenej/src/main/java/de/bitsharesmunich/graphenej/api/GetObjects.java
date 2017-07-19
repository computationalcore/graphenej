package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.AccountOptions;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.Authority;
import de.bitsharesmunich.graphenej.GrapheneObject;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BitAssetData;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 1/8/17.
 */
public class GetObjects extends BaseGrapheneHandler {
    private List<String> ids;

    private boolean mOneTime;

    public GetObjects(List<String> ids, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.ids = ids;
        this.mOneTime = oneTime;
    }

    public GetObjects(List<String> ids, WitnessResponseListener listener){
        this(ids, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> subParams = new ArrayList<>();
        for(String id : this.ids){
            subParams.add(id);
        }
        params.add(subParams);
        ApiCall apiCall = new ApiCall(0, RPC.GET_OBJECTS, params, RPC.VERSION, 0);
        websocket.sendText(apiCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println("<< "+frame.getPayloadText());
        }
        String response = frame.getPayloadText();
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
        gsonBuilder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountFullDeserializer());
        gsonBuilder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
        gsonBuilder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
        Gson gson = gsonBuilder.create();

        List<GrapheneObject> parsedResult = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray resultArray = parser.parse(response).getAsJsonObject().get(WitnessResponse.KEY_RESULT).getAsJsonArray();
        for(JsonElement element : resultArray){
            String id = element.getAsJsonObject().get(GrapheneObject.KEY_ID).getAsString();
            GrapheneObject grapheneObject = new GrapheneObject(id);
            switch (grapheneObject.getObjectType()){
                case ASSET_OBJECT:
                    Asset asset = gson.fromJson(element, Asset.class);
                    parsedResult.add(asset);
                    break;
                case ACCOUNT_OBJECT:
                    UserAccount account = gson.fromJson(element, UserAccount.class);
                    parsedResult.add(account);
                    break;
                case ASSET_BITASSET_DATA:
                    Type BitAssetDataType = new TypeToken<WitnessResponse<List<BitAssetData>>>(){}.getType();
                    WitnessResponse<List<BitAssetData>> witnessResponse = gsonBuilder.create().fromJson(response, BitAssetDataType);
                    BitAssetData bitAssetData = witnessResponse.result.get(0);
                    parsedResult.add(bitAssetData);
            }
        }

        WitnessResponse<List<GrapheneObject>> output = new WitnessResponse<>();
        output.result = parsedResult;
        mListener.onSuccess(output);
        if(mOneTime){
            websocket.disconnect();
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">> "+frame.getPayloadText());
        }
    }
}
