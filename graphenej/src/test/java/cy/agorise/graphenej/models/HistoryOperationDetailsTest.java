package cy.agorise.graphenej.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Type;

import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.Extensions;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.android.DeserializationMap;
import cy.agorise.graphenej.Memo;

public class HistoryOperationDetailsTest {

    @Test
    public void testDeserialization(){
        String text = "{\"id\":5,\"jsonrpc\":\"2.0\",\"result\":{\"total_count\":2,\"operation_history_objs\":[{\"id\":\"1.11.5701809\",\"op\":[0,{\"fee\":{\"amount\":264174,\"asset_id\":\"1.3.0\"},\"from\":\"1.2.99700\",\"to\":\"1.2.138632\",\"amount\":{\"amount\":20000,\"asset_id\":\"1.3.120\"},\"extensions\":[]}],\"result\":[0,{}],\"block_num\":11094607,\"trx_in_block\":0,\"op_in_trx\":0,\"virtual_op\":31767},{\"id\":\"1.11.5701759\",\"op\":[0,{\"fee\":{\"amount\":264174,\"asset_id\":\"1.3.0\"},\"from\":\"1.2.99700\",\"to\":\"1.2.138632\",\"amount\":{\"amount\":10000000,\"asset_id\":\"1.3.0\"},\"extensions\":[]}],\"result\":[0,{}],\"block_num\":11094501,\"trx_in_block\":0,\"op_in_trx\":0,\"virtual_op\":31717}]}}\n";
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new DeserializationMap.SkipAccountOptionsStrategy(), new DeserializationMap.SkipAssetOptionsStrategy())
                .registerTypeAdapter(BaseOperation.class, new BaseOperation.OperationDeserializer())
                .registerTypeAdapter(OperationHistory.class, new OperationHistory.OperationHistoryDeserializer())
                .registerTypeAdapter(Memo.class, new Memo.MemoSerializer())
                .registerTypeAdapter(Extensions.class, new Extensions.ExtensionsDeserializer())
                .registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .create();

        Type GetAccountHistoryByOperationsResponse = new TypeToken<JsonRpcResponse<HistoryOperationDetail>>(){}.getType();
        JsonRpcResponse<HistoryOperationDetail> response = gson.fromJson(text, GetAccountHistoryByOperationsResponse);
        Assert.assertNotNull(response.result);
        Assert.assertNotNull(response.result.operation_history_objs);
    }
}
