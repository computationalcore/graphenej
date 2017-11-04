package cy.agorise.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.LimitOrder;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *  Class that implements get_limit_orders request handler.
 *
 *  Get limit orders in a given market.
 *
 *  The request returns the limit orders, ordered from least price to greatest
 *
 *  @see <a href="https://goo.gl/5sRTRq">get_limit_orders API doc</a>
 *
 */
public class GetLimitOrders extends BaseGrapheneHandler {

    private String a;
    private String b;
    private int limit;
    private WitnessResponseListener mListener;

    private boolean mOneTime;


    /**
     * Default Constructor
     *
     * @param a             id of asset being sold
     * @param b             id of asset being purchased
     * @param limit         maximum number of orders to retrieve
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetLimitOrders(String a, String b, int limit, boolean oneTime, WitnessResponseListener listener) {
        super(listener);
        this.a = a;
        this.b = b;
        this.limit = limit;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param a             id of asset being sold
     * @param b             id of asset being purchased
     * @param limit         maximum number of orders to retrieve
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetLimitOrders(String a, String b, int limit, WitnessResponseListener listener) {
        this(a, b, limit, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.a);
        accountParams.add(this.b);
        accountParams.add(this.limit);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_LIMIT_ORDERS, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println("<<< "+frame.getPayloadText());
        try {
            String response = frame.getPayloadText();
            GsonBuilder builder = new GsonBuilder();

            Type GetLimitOrdersResponse = new TypeToken<WitnessResponse<List<LimitOrder>>>() {}.getType();
            builder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
            builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
            builder.registerTypeAdapter(LimitOrder.class, new LimitOrder.LimitOrderDeserializer());
            WitnessResponse<List<LimitOrder>> witnessResponse = builder.create().fromJson(response, GetLimitOrdersResponse);
            if (witnessResponse.error != null) {
                this.mListener.onError(witnessResponse.error);
            } else {
                this.mListener.onSuccess(witnessResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mOneTime){
            websocket.disconnect();
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">>> "+frame.getPayloadText());
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        if(mOneTime){
            websocket.disconnect();
        }
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        if(mOneTime){
            websocket.disconnect();
        }
    }
}
