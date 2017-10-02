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

import cy.agorise.graphenej.MarketTrade;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *  Class that implements get_trade_history request handler.
 *
 *  Get recent trades for the market assetA:assetB for a time interval
 *  Note: Currently, timezone offsets are not supported. The time must be UTC.
 *
 *  The request returns the all trades of the passed pair of asset at a specific time interval.
 *
 *  @see <a href="https://goo.gl/Y1x3bE">get_trade_history API doc</a>
 *
 */
public class GetTradeHistory extends BaseGrapheneHandler {

    private String a;
    private String b;
    private String toTime;
    private String fromTime;
    private int limit;
    private WitnessResponseListener mListener;

    private boolean mOneTime;

    /**
     * Constructor
     *
     * @param a             name of the first asset
     * @param b             name of the second asset
     * @param toTime        stop time as a UNIX timestamp
     * @param fromTime      start time as a UNIX timestamp
     * @param limit         number of transactions to retrieve, capped at 100
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetTradeHistory(String a, String b, String toTime, String fromTime,int limit, boolean oneTime, WitnessResponseListener listener) {
        super(listener);
        this.a = a;
        this.b = b;
        this.toTime = toTime;
        this.fromTime = fromTime;
        this.limit = limit;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param a             name of the first asset
     * @param b             name of the second asset
     * @param toTime        stop time as a UNIX timestamp
     * @param fromTime      start time as a UNIX timestamp
     * @param limit         number of transactions to retrieve, capped at 100
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetTradeHistory(String a, String b, String toTime, String fromTime,int limit, WitnessResponseListener listener) {
        this(a, b, toTime, fromTime, limit, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.a);
        accountParams.add(this.b);
        accountParams.add(this.toTime);
        accountParams.add(this.fromTime);
        accountParams.add(this.limit);

        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_TRADE_HISTORY, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if (frame.isTextFrame()) {
            System.out.println("<<< " + frame.getPayloadText());
        }
        try {
            String response = frame.getPayloadText();
            GsonBuilder builder = new GsonBuilder();

            Type GetTradeHistoryResponse = new TypeToken<WitnessResponse<List<MarketTrade>>>() {
            }.getType();
            WitnessResponse<List<MarketTrade>> witnessResponse = builder.create().fromJson(response, GetTradeHistoryResponse);
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
        if (frame.isTextFrame()) {
            System.out.println(">>> " + frame.getPayloadText());
        }
    }
}
