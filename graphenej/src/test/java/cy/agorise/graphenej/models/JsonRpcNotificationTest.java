package cy.agorise.graphenej.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.Memo;
import cy.agorise.graphenej.operations.CustomOperation;
import cy.agorise.graphenej.operations.LimitOrderCreateOperation;
import cy.agorise.graphenej.operations.TransferOperation;

public class JsonRpcNotificationTest {

    private String text = "{\"method\":\"notice\",\"params\":[3,[[{\"id\":\"2.1.0\",\"head_block_number\":30071834,\"head_block_id\":\"01cadc1a5f3f517e2eba9588111aef3af3c59916\",\"time\":\"2018-08-30T18:19:45\",\"current_witness\":\"1.6.74\",\"next_maintenance_time\":\"2018-08-30T19:00:00\",\"last_budget_time\":\"2018-08-30T18:00:00\",\"witness_budget\":80800000,\"accounts_registered_this_interval\":9,\"recently_missed_count\":0,\"current_aslot\":30228263,\"recent_slots_filled\":\"340282366920938463463374607431768211455\",\"dynamic_flags\":0,\"last_irreversible_block_num\":30071813}]]]}";

    @Test
    public void failResponseDeserialization(){
        Gson gson = new Gson();
        JsonRpcResponse<?> response = gson.fromJson(text, JsonRpcResponse.class);
        // The result field of this de-serialized object should be null
        Assert.assertNull(response.result);
    }

    @Test
    public void succeedNotificationDeserialization(){
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Transaction.class, new Transaction.TransactionDeserializer())
            .registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer())
            .registerTypeAdapter(LimitOrderCreateOperation.class, new LimitOrderCreateOperation.LimitOrderCreateDeserializer())
            .registerTypeAdapter(CustomOperation.class, new CustomOperation.CustomOperationDeserializer())
            .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
            .registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer())
            .registerTypeAdapter(DynamicGlobalProperties.class, new DynamicGlobalProperties.DynamicGlobalPropertiesDeserializer())
            .registerTypeAdapter(Memo.class, new Memo.MemoDeserializer())
            .registerTypeAdapter(OperationHistory.class, new OperationHistory.OperationHistoryDeserializer())
            .registerTypeAdapter(JsonRpcNotification.class, new JsonRpcNotification.JsonRpcNotificationDeserializer())
            .create();
        JsonRpcNotification notification = gson.fromJson(text, JsonRpcNotification.class);
        // Should deserialize a 'params' array with 2 elements
        Assert.assertEquals(2, notification.params.size());
        // The first element should be the number 3
        Assert.assertEquals(3, notification.params.get(0));
        ArrayList<Serializable> secondArgument = (ArrayList<Serializable>) notification.params.get(1);
        // The second element should be an array of length 1
        Assert.assertEquals(1, secondArgument.size());
        // Extracting the payload, which should be in itself another array
        DynamicGlobalProperties payload = (DynamicGlobalProperties) secondArgument.get(0);
        // Dynamic global properties head_block_number should match
        Assert.assertEquals(30071834, payload.head_block_number);
    }
}
