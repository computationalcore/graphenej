package cy.agorise.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cy.agorise.graphenej.AccountOptions;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.Authority;
import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.AssetFeed;
import cy.agorise.graphenej.models.BitAssetData;
import cy.agorise.graphenej.models.ReportedAssetFeed;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *
 *  Class that implements get_objects request handler.
 *
 *  Get the objects corresponding to the provided IDs.
 *
 *  The response returns a list of objects retrieved, in the order they are mentioned in ids
 *
 *  @see <a href="https://goo.gl/isRfeg">get_objects API doc</a>
 *
 */
public class GetObjects extends BaseGrapheneHandler {
    private List<String> ids;

    private boolean mOneTime;

    /**
     * Default Constructor
     *
     * @param ids           list of IDs of the objects to retrieve
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetObjects(List<String> ids, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.ids = ids;
        this.mOneTime = oneTime;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param ids           list of IDs of the objects to retrieve
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetObjects(List<String> ids, WitnessResponseListener listener){
        this(ids, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> subParams = new ArrayList<>();
        subParams.addAll(this.ids);
        params.add(subParams);
        ApiCall apiCall = new ApiCall(0, RPC.CALL_GET_OBJECTS, params, RPC.VERSION, 0);
        websocket.sendText(apiCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println("<< "+frame.getPayloadText());
        }
        String response = frame.getPayloadText();
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(BitAssetData.class, new BitAssetData.BitAssetDataDeserializer());
        gsonBuilder.registerTypeAdapter(AssetFeed.class, new AssetFeed.AssetFeedDeserializer());
        gsonBuilder.registerTypeAdapter(ReportedAssetFeed.class, new ReportedAssetFeed.ReportedAssetFeedDeserializer());
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
        gsonBuilder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountFullDeserializer());
        gsonBuilder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
        gsonBuilder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
        Gson gson = gsonBuilder.create();

        List<GrapheneObject> parsedResult = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray resultArray = parser.parse(response).getAsJsonObject().get(WitnessResponse.KEY_RESULT).getAsJsonArray();
        for(int i = 0; i < resultArray.size(); i++){
            JsonElement element = resultArray.get(i);
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
                    BitAssetData bitAssetData = gson.fromJson(element, BitAssetData.class);
                    parsedResult.add(bitAssetData);
                    break;
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
