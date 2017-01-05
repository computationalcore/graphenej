package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WebSocketAdapter class used to send a request a 'list_assets' API call to the witness node.
 *
 * @see: <a href="http://docs.bitshares.org/development/namespaces/app.html"></a>
 *
 * The API imposes a limit of of 100 assets per request, but if the user of this class wants
 * to get a list of all assets, the LIST_ALL constant must be used as second argument in the
 * constructor. Internally we are going to perform multiple calls in order to satisfy the user's
 * request.
 *
 * Created by nelson on 1/5/17.
 */
public class ListAssets extends BaseGrapheneHandler {
    /**
     * Constant that must be used as argument to the constructor of this class to indicate
     * that the user wants to get all existing assets.
     */
    public static final int LIST_ALL = -1;

    /**
     * Internal constant used to represent the maximum limit of assets retrieved in one call.
     */
    private final int MAX_BATCH_SIZE = 100;

    private List<Asset> assets;
    private String lowerBound;
    private int limit;
    private int requestCounter = 0;

    /**
     * Constructor
     * @param lowerBoundSymbol: Lower bound of symbol names to retrieve
     * @param limit: Maximum number of assets to fetch, if the constant LIST_ALL
     *             is passed, all existing assets will be retrieved.
     */
    public ListAssets(String lowerBoundSymbol, int limit, WitnessResponseListener listener){
        super(listener);
        this.lowerBound = lowerBoundSymbol;
        this.limit = limit;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(this.lowerBound);
        if(limit > MAX_BATCH_SIZE || limit == LIST_ALL){
            params.add(MAX_BATCH_SIZE);
        }else{
            params.add(this.limit);
        }
        ApiCall apiCall = new ApiCall(0, RPC.CALL_LIST_ASSETS, params, RPC.VERSION, 0);
        websocket.sendText(apiCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Type LookupAssetSymbolsResponse = new TypeToken<WitnessResponse<List<Asset>>>(){}.getType();
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
        WitnessResponse<List<Asset>> witnessResponse = gsonBuilder.create().fromJson(response, LookupAssetSymbolsResponse);
        if(this.limit != LIST_ALL && this.limit < MAX_BATCH_SIZE){
            // If the requested number of assets was below
            // the limit, we just call the listener.
            mListener.onSuccess(witnessResponse);
            websocket.disconnect();
        }else{
            // Updating counter to keep track of how many batches we already retrieved.
            requestCounter++;
            if(this.assets == null){
                this.assets = new ArrayList<>();
            }
            this.assets.addAll(witnessResponse.result);

            // Checking to see if we're done
            if(this.limit == LIST_ALL && witnessResponse.result.size() < MAX_BATCH_SIZE){
                // In case we requested all assets, we might be in the last round whenever
                // we got less than the requested amount.
                witnessResponse.result = this.assets;
                mListener.onSuccess(witnessResponse);
                websocket.disconnect();
            }else if(this.assets.size() == this.limit){
                // We already have the required amount of assets
                witnessResponse.result = this.assets;
                mListener.onSuccess(witnessResponse);
                websocket.disconnect();
            }else{
                // We still need to fetch some more assets
                this.lowerBound = this.assets.get(this.assets.size() - 1).getSymbol();
                int nextBatch = this.limit == LIST_ALL ? MAX_BATCH_SIZE : this.limit - (MAX_BATCH_SIZE * requestCounter);
                ArrayList<Serializable> params = new ArrayList<>();
                params.add(this.lowerBound);
                params.add(nextBatch);
                ApiCall apiCall = new ApiCall(0, RPC.CALL_LIST_ASSETS, params, RPC.VERSION, 0);
                websocket.sendText(apiCall.toJsonString());
            }
        }
    }
}
