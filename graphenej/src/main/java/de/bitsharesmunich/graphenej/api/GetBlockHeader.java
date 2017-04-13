package de.bitsharesmunich.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.BlockHeader;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 12/13/16.
 */
public class GetBlockHeader extends BaseGrapheneHandler {

    // Sequence of message ids
    private final static int LOGIN_ID = 1;
    private final static int GET_DATABASE_ID = 2;
    private final static int GET_BLOCK_HEADER_ID = 3;

    private long blockNumber;
    private WitnessResponseListener mListener;

    private int currentId = LOGIN_ID;
    private int apiId = -1;

    public GetBlockHeader(long blockNumber, WitnessResponseListener listener){
        super(listener);
        this.blockNumber = blockNumber;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        System.out.println("<<< "+response);

        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            websocket.disconnect();
        }else {
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if(baseResponse.id == LOGIN_ID){
                ApiCall getDatabaseId = new ApiCall(1, RPC.CALL_DATABASE, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(getDatabaseId.toJsonString());
            }else if(baseResponse.id == GET_DATABASE_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                apiId = witnessResponse.result.intValue();

                ArrayList<Serializable> params = new ArrayList<>();
                String blockNum = String.format("%d", this.blockNumber);
                params.add(blockNum);

                ApiCall loginCall = new ApiCall(apiId, RPC.CALL_GET_BLOCK_HEADER, params, RPC.VERSION, currentId);
                websocket.sendText(loginCall.toJsonString());
            }else if(baseResponse.id == GET_BLOCK_HEADER_ID){
                Type RelativeAccountHistoryResponse = new TypeToken<WitnessResponse<BlockHeader>>(){}.getType();
                WitnessResponse<BlockHeader> transfersResponse = gson.fromJson(response, RelativeAccountHistoryResponse);
                mListener.onSuccess(transfersResponse);
                websocket.disconnect();
            }
        }

    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}
