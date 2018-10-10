package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**  Class that implements get_limit_orders request handler.
 *
 *  Get limit orders in a given market.
 *
 *  The request returns the limit orders, ordered from least price to greatest
 *
 *  @see <a href="https://goo.gl/5sRTRq">get_limit_orders API doc</a>
 *
 */
public class GetLimitOrders implements ApiCallable  {

    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    private String a;
    private String b;
    private int limit;

    public GetLimitOrders(String a, String b, int limit){
        this.a = a;
        this.b = b;
        this.limit = limit;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> parameters = new ArrayList<>();
        parameters.add(a);
        parameters.add(b);
        parameters.add(limit);
        return new ApiCall(apiId, RPC.CALL_GET_LIMIT_ORDERS, parameters, RPC.VERSION, sequenceId);
    }
}
