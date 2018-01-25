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

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *  Class that implements lookup_asset_symbols request handler.
 *
 *  Get the assets corresponding to the provided IDs.
 *
 *  The response returns the assets corresponding to the provided symbols or IDs.
 *
 *  @see <a href="https://goo.gl/WvREGV">lookup_asset_symbols API doc</a>
 */
public class LookupAssetSymbols extends BaseGrapheneHandler {
    private WitnessResponseListener mListener;
    private List<? extends Object> assets;

    private boolean mOneTime;

    /**
     * Default Constructor
     *
     * @param assets        list of the assets to retrieve
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public LookupAssetSymbols(List<? extends Object> assets, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.assets = assets;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param assets        list of the assets to retrieve
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public LookupAssetSymbols(List<Object> assets, WitnessResponseListener listener){
        this(assets, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<String> subArray = new ArrayList<>();
        for(int i = 0; i < assets.size(); i++){
            Object obj = assets.get(i);
            if(obj instanceof String){
                subArray.add((String) obj);
            }else{
                subArray.add(((Asset) obj).getObjectId());
            }
            params.add(subArray);
        }
        ApiCall loginCall = new ApiCall(0, RPC.CALL_LOOKUP_ASSET_SYMBOLS, params, RPC.VERSION, 0);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        System.out.println("<<< "+response);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Type LookupAssetSymbolsResponse = new TypeToken<WitnessResponse<List<Asset>>>(){}.getType();
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
        WitnessResponse<List<Asset>> witnessResponse = gsonBuilder.create().fromJson(response, LookupAssetSymbolsResponse);
        mListener.onSuccess(witnessResponse);
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
