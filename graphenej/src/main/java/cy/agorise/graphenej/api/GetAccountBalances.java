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
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *  Class that implements get_account_balances request handler.
 *
 *  Get an account’s balances in various assets.
 *
 *  The response returns the balances of the account
 *
 *  @see <a href="https://goo.gl/faFdey">get_account_balances API doc</a>
 *
 */
public class GetAccountBalances extends BaseGrapheneHandler {

    private UserAccount mUserAccount;
    private List<Asset> mAssetList;
    private boolean mOneTime;

    /**
     * Default Constructor
     *
     * @param userAccount   account to get balances for
     * @param assets        list of the assets to get balances of; if empty, get all assets account
     *                      has a balance in
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccountBalances(UserAccount userAccount, List<Asset> assets, boolean oneTime, WitnessResponseListener listener) {
        super(listener);
        this.mUserAccount = userAccount;
        this.mAssetList = assets;
        this.mOneTime = oneTime;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param userAccount   account to get balances for
     * @param assets        list of the assets to get balances of; if empty, get all assets account
     *                      has a balance in
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccountBalances(UserAccount userAccount, List<Asset> assets, WitnessResponseListener listener) {
        this(userAccount, assets, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> assetList = new ArrayList<>();
        if(mAssetList != null){
            for(Asset asset : mAssetList){
                assetList.add(asset.getObjectId());
            }
        }
        params.add(mUserAccount.getObjectId());
        params.add(assetList);
        ApiCall apiCall = new ApiCall(0, RPC.GET_ACCOUNT_BALANCES, params, RPC.VERSION, requestId);
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
