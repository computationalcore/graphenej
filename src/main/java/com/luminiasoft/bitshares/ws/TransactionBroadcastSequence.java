package com.luminiasoft.bitshares.ws;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.BaseOperation;
import com.luminiasoft.bitshares.BlockData;
import com.luminiasoft.bitshares.RPC;
import com.luminiasoft.bitshares.Transaction;
import com.luminiasoft.bitshares.Transfer;
import com.luminiasoft.bitshares.TransferTransactionBuilder;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
import com.luminiasoft.bitshares.models.ApiCall;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.DynamicGlobalProperties;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class that will handle the transaction publication procedure.
 */
public class TransactionBroadcastSequence extends WebSocketAdapter {
    private final String TAG = this.getClass().getName();

    private final static int LOGIN_ID = 1;
    private final static int GET_NETWORK_BROADCAST_ID = 2;
    private final static int GET_NETWORK_DYNAMIC_PARAMETERS = 3;
    private final static int BROADCAST_TRANSACTION = 4;
    public final static int EXPIRATION_TIME = 30;

    private Transaction transaction;
    private long expirationTime;
    private String headBlockId;
    private long headBlockNumber;
    private WitnessResponseListener mListener;

    private int currentId = 1;
    private int broadcastApiId = -1;
    private int retries = 0;

    /**
     * Constructor of this class. The ids required
     * @param transaction: The transaction to be broadcasted.
     * @param listener: A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public TransactionBroadcastSequence(Transaction transaction, WitnessResponseListener listener){
        this.transaction = transaction;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, "2.0", currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null && baseResponse.error.message.indexOf("is_canonical") == -1){
            mListener.onError(baseResponse.error);
            websocket.disconnect();
        }else{
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if(baseResponse.id == LOGIN_ID){
                ApiCall networkApiIdCall = new ApiCall(1, RPC.CALL_NETWORK_BROADCAST, emptyParams, "2.0", currentId);
                websocket.sendText(networkApiIdCall.toJsonString());
            }else if(baseResponse.id == GET_NETWORK_BROADCAST_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                broadcastApiId = witnessResponse.result;

                ApiCall getDynamicParametersCall = new ApiCall(0, RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES, emptyParams, "2.0", currentId);
                websocket.sendText(getDynamicParametersCall.toJsonString());
            }else if(baseResponse.id == GET_NETWORK_DYNAMIC_PARAMETERS){
                Type DynamicGlobalPropertiesResponse = new TypeToken<WitnessResponse<DynamicGlobalProperties>>(){}.getType();
                WitnessResponse<DynamicGlobalProperties> witnessResponse = gson.fromJson(response, DynamicGlobalPropertiesResponse);
                DynamicGlobalProperties dynamicProperties = witnessResponse.result;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = dateFormat.parse(dynamicProperties.time);

                // Obtained block data
                expirationTime = (date.getTime() / 1000) + EXPIRATION_TIME;
                headBlockId = dynamicProperties.head_block_id;
                headBlockNumber = dynamicProperties.head_block_number;

                ArrayList<Serializable> transactionList = new ArrayList<>();
                transactionList.add(transaction);
                ApiCall call = new ApiCall(broadcastApiId,
                        RPC.CALL_BROADCAST_TRANSACTION,
                        transactionList,
                        "2.0",
                        currentId);

                // Finally sending transaction
                websocket.sendText(call.toJsonString());
            }else if(baseResponse.id >= BROADCAST_TRANSACTION){
                Type WitnessResponseType = new TypeToken<WitnessResponse<String>>(){}.getType();
                WitnessResponse<WitnessResponse<String>> witnessResponse = gson.fromJson(response, WitnessResponseType);
                if(witnessResponse.error == null){
                    mListener.onSuccess(witnessResponse);
                    websocket.disconnect();
                }else{
                    if(witnessResponse.error.message.indexOf("is_canonical") != -1 && retries < 10){
                        /*
                        * This is a very ugly hack, but it will do for now.
                        *
                        * The issue is that the witness is complaining about the signature not
                        * being canonical even though the bitcoinj ECKey.ECDSASignature.isCanonical()
                        * method says it is! We'll have to dive deeper into this issue and avoid
                        * this error altogether
                        *
                        * But this MUST BE FIXED! Since this hack will only work for transactions
                        * with ONE transfer operation.
                        */
                        retries++;
                        List<BaseOperation> operations = this.transaction.getOperations();
                        Transfer transfer = (Transfer) operations.get(0);
                        transaction = new TransferTransactionBuilder()
                                .setSource(transfer.getFrom())
                                .setDestination(transfer.getTo())
                                .setAmount(transfer.getAmount())
                                .setFee(transfer.getFee())
                                .setBlockData(new BlockData(headBlockNumber, headBlockId, expirationTime + EXPIRATION_TIME))
                                .setPrivateKey(transaction.getPrivateKey())
                                .build();
                        ArrayList<Serializable> transactionList = new ArrayList<>();
                        transactionList.add(transaction);
                        ApiCall call = new ApiCall(broadcastApiId,
                                RPC.CALL_BROADCAST_TRANSACTION,
                                transactionList,
                                "2.0",
                                currentId);
                        websocket.sendText(call.toJsonString());
                    }else{
                        mListener.onError(witnessResponse.error);
                        websocket.disconnect();
                    }
                }
            }
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}