package de.bitsharesmunich.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.RPC;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.ApiCall;
import de.bitsharesmunich.graphenej.models.BitAssetData;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nelson on 1/8/17.
 */
public class GetObjects extends BaseGrapheneHandler {
    private List<String> ids;

    public GetObjects(List<String> ids, WitnessResponseListener listener){
        super(listener);
        this.ids = ids;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> subParams = new ArrayList<>();
        for(String id : this.ids){
            subParams.add(id);
        }
        params.add(subParams);
        ApiCall apiCall = new ApiCall(0, RPC.GET_OBJECTS, params, RPC.VERSION, 0);
        websocket.sendText(apiCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println("<< "+frame.getPayloadText());
        }
        String response = frame.getPayloadText();
        GsonBuilder gsonBuilder = new GsonBuilder();

        //TODO: Uncomment this line after the deserializer is implemented.
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
//        gsonBuilder.registerTypeAdapter(BitAssetData.class, new BitAssetData.BitAssetDeserializer());

        // Only homogeneus array is currently supported
        if(ids.get(0).split("\\.")[1].equals("4")){
            Type BitAssetDataType = new TypeToken<WitnessResponse<List<BitAssetData>>>(){}.getType();
            WitnessResponse<List<BitAssetData>> witnessResponse = gsonBuilder.create().fromJson(response, BitAssetDataType);
            mListener.onSuccess(witnessResponse);
        }
        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">> "+frame.getPayloadText());
        }
    }
}
