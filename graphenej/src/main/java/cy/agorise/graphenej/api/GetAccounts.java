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

import cy.agorise.graphenej.AccountOptions;
import cy.agorise.graphenej.Authority;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.AccountProperties;
import cy.agorise.graphenej.models.ApiCall;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 *  Class that implements get_accounts request handler.
 *
 *  Get a list of accounts by ID.
 *
 *  The response returns the accounts corresponding to the provided IDs.
 *
 *  @see <a href="https://goo.gl/r5RqKG">get_accounts API doc</a>
 */
public class GetAccounts extends BaseGrapheneHandler {
    private String accountId;
    private List<UserAccount> userAccounts;
    private WitnessResponseListener mListener;
    private boolean mOneTime;

    /**
     * Constructor for one account only.
     *
     * @param accountId     ID of the account to retrieve
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccounts(String accountId, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.accountId = accountId;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Constructor for account list.
     *
     * @param accounts      list with the accounts to retrieve
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccounts(List<UserAccount> accounts, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.userAccounts = accounts;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response. (Account based)
     *
     * @param accountId     ID of the account to retrieve
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccounts(String accountId, WitnessResponseListener listener){
        this(accountId, true, listener);
    }

    /**
     * Using this constructor the WebSocket connection closes after the response. (Account List
     * based)
     *
     * @param accounts      list with the accounts to retrieve
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccounts(List<UserAccount> accounts, WitnessResponseListener listener){
        this(accounts, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList();
        ArrayList<Serializable> accountIds = new ArrayList();
        if(accountId == null){
            for(UserAccount account : userAccounts) {
                accountIds.add(account.getObjectId());
            }
        }else{
            accountIds.add(accountId);
        }
        params.add(accountIds);
        ApiCall getAccountByAddress = new ApiCall(0, RPC.CALL_GET_ACCOUNTS, params, RPC.VERSION, (int) requestId);
        websocket.sendText(getAccountByAddress.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        GsonBuilder builder = new GsonBuilder();

        Type GetAccountByAddressResponse = new TypeToken<WitnessResponse<List<AccountProperties>>>() {}.getType();
        builder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
        builder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
        WitnessResponse<List<AccountProperties>> witnessResponse = builder.create().fromJson(response, GetAccountByAddressResponse);

        if (witnessResponse.error != null) {
            this.mListener.onError(witnessResponse.error);
        } else {
            this.mListener.onSuccess(witnessResponse);
        }
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
