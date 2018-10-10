package cy.agorise.graphenej.models;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.Test;

public class JsonRpcResponseTest {

    @Test
    public void deserializeJsonRpcResponse(){
        String text = "{\"id\":4,\"jsonrpc\":\"2.0\",\"result\":[{\"id\":\"2.1.0\",\"head_block_number\":30071833,\"head_block_id\":\"01cadc1964cb04ab551463e26033ab0f159bc8e1\",\"time\":\"2018-08-30T18:19:42\",\"current_witness\":\"1.6.71\",\"next_maintenance_time\":\"2018-08-30T19:00:00\",\"last_budget_time\":\"2018-08-30T18:00:00\",\"witness_budget\":80900000,\"accounts_registered_this_interval\":9,\"recently_missed_count\":0,\"current_aslot\":30228262,\"recent_slots_filled\":\"340282366920938463463374607431768211455\",\"dynamic_flags\":0,\"last_irreversible_block_num\":30071813}]}";
        Gson gson = new Gson();
        JsonRpcResponse<?> response = gson.fromJson(text, JsonRpcResponse.class);
        System.out.println("response: "+response.result);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.result);
    }
}
