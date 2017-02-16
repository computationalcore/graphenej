package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nelson on 1/13/17.
 */
public class GetAccountBalances extends BaseGrapheneHandler {

    private UserAccount mUserAccount;
    private List<Asset> mAssetList;

    public GetAccountBalances(UserAccount userAccount, List<Asset> assets, WitnessResponseListener listener) {
        super(listener);
        this.mUserAccount = userAccount;
        this.mAssetList = assets;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> assetList = new ArrayList<>();
        for(Asset asset : mAssetList){
            assetList.add(asset.getObjectId());
        }
        params.add(mUserAccount.getObjectId());
        params.add(assetList);
        ApiCall apiCall = new ApiCall(0, RPC.GET_ACCOUNT_BALANCES, params, RPC.VERSION, 0);
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

        Type WitnessResponseType = new TypeToken<WitnessResponse<List<AssetAmount>>>(){}.getType();
        WitnessResponse<List<AssetAmount>> witnessResponse = gsonBuilder.create().fromJson(response, WitnessResponseType);
        mListener.onSuccess(witnessResponse);
        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">> "+frame.getPayloadText());
        }
    }
}
