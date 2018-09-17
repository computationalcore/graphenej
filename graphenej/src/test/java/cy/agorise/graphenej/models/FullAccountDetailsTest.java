package cy.agorise.graphenej.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import cy.agorise.graphenej.AccountOptions;
import cy.agorise.graphenej.Authority;
import cy.agorise.graphenej.Memo;

public class FullAccountDetailsTest {

    @Test
    public void testDeserialization(){
        String serialized = "{\"id\":0,\"jsonrpc\":\"2.0\",\"result\":[[\"bilthon-1\",{\"account\":{\"id\":\"1.2.139205\",\"membership_expiration_date\":\"1970-01-01T00:00:00\",\"registrar\":\"1.2.117600\",\"referrer\":\"1.2.90200\",\"lifetime_referrer\":\"1.2.90200\",\"network_fee_percentage\":2000,\"lifetime_referrer_fee_percentage\":3000,\"referrer_rewards_percentage\":9000,\"name\":\"bilthon-1\",\"owner\":{\"weight_threshold\":1,\"account_auths\":[],\"key_auths\":[[\"BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY\",1]],\"address_auths\":[]},\"active\":{\"weight_threshold\":1,\"account_auths\":[],\"key_auths\":[[\"BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY\",1]],\"address_auths\":[]},\"options\":{\"memo_key\":\"BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY\",\"voting_account\":\"1.2.5\",\"num_witness\":0,\"num_committee\":0,\"votes\":[],\"extensions\":[]},\"statistics\":\"2.6.139205\",\"whitelisting_accounts\":[],\"blacklisting_accounts\":[],\"whitelisted_accounts\":[],\"blacklisted_accounts\":[],\"owner_special_authority\":[0,{}],\"active_special_authority\":[0,{}],\"top_n_control_flags\":0},\"statistics\":{\"id\":\"2.6.139205\",\"owner\":\"1.2.139205\",\"name\":\"bilthon-1\",\"most_recent_op\":\"2.9.6668024\",\"total_ops\":3,\"removed_ops\":0,\"total_core_in_orders\":0,\"core_in_balance\":71279,\"has_cashback_vb\":false,\"is_voting\":false,\"lifetime_fees_paid\":28721,\"pending_fees\":0,\"pending_vested_fees\":0},\"registrar_name\":\"bitshares-munich-faucet\",\"referrer_name\":\"bitshares-munich\",\"lifetime_referrer_name\":\"bitshares-munich\",\"votes\":[],\"balances\":[{\"id\":\"2.5.44951\",\"owner\":\"1.2.139205\",\"asset_type\":\"1.3.0\",\"balance\":71279,\"maintenance_flag\":false}],\"vesting_balances\":[],\"limit_orders\":[],\"call_orders\":[],\"settle_orders\":[],\"proposals\":[],\"assets\":[],\"withdraws\":[]}],[\"bilthon-2\",{\"account\":{\"id\":\"1.2.139207\",\"membership_expiration_date\":\"1970-01-01T00:00:00\",\"registrar\":\"1.2.117600\",\"referrer\":\"1.2.90200\",\"lifetime_referrer\":\"1.2.90200\",\"network_fee_percentage\":2000,\"lifetime_referrer_fee_percentage\":3000,\"referrer_rewards_percentage\":9000,\"name\":\"bilthon-2\",\"owner\":{\"weight_threshold\":1,\"account_auths\":[],\"key_auths\":[[\"BTS7gD2wtSauXpSCBin1rYctBcPWeZieX7YrVk1DuQpg9peczSqTv\",1]],\"address_auths\":[]},\"active\":{\"weight_threshold\":1,\"account_auths\":[],\"key_auths\":[[\"BTS7gD2wtSauXpSCBin1rYctBcPWeZieX7YrVk1DuQpg9peczSqTv\",1]],\"address_auths\":[]},\"options\":{\"memo_key\":\"BTS7gD2wtSauXpSCBin1rYctBcPWeZieX7YrVk1DuQpg9peczSqTv\",\"voting_account\":\"1.2.5\",\"num_witness\":0,\"num_committee\":0,\"votes\":[],\"extensions\":[]},\"statistics\":\"2.6.139207\",\"whitelisting_accounts\":[],\"blacklisting_accounts\":[],\"whitelisted_accounts\":[],\"blacklisted_accounts\":[],\"owner_special_authority\":[0,{}],\"active_special_authority\":[0,{}],\"top_n_control_flags\":0},\"statistics\":{\"id\":\"2.6.139207\",\"owner\":\"1.2.139207\",\"name\":\"bilthon-2\",\"most_recent_op\":\"2.9.6159244\",\"total_ops\":1,\"removed_ops\":0,\"total_core_in_orders\":0,\"core_in_balance\":0,\"has_cashback_vb\":false,\"is_voting\":false,\"lifetime_fees_paid\":0,\"pending_fees\":0,\"pending_vested_fees\":0},\"registrar_name\":\"bitshares-munich-faucet\",\"referrer_name\":\"bitshares-munich\",\"lifetime_referrer_name\":\"bitshares-munich\",\"votes\":[],\"balances\":[],\"vesting_balances\":[],\"limit_orders\":[],\"call_orders\":[],\"settle_orders\":[],\"proposals\":[],\"assets\":[],\"withdraws\":[]}]]}";
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(FullAccountDetails.class, new FullAccountDetails.FullAccountDeserializer())
                .registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer())
                .registerTypeAdapter(Memo.class, new Memo.MemoDeserializer())
                .registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer())
                .create();
        Type FullAccountDetailsResponse = new TypeToken<JsonRpcResponse<List<FullAccountDetails>>>() {}.getType();
        JsonRpcResponse<List<FullAccountDetails>> response = gson.fromJson(serialized, FullAccountDetailsResponse);
        Assert.assertNotNull(response.result);
        Assert.assertNull(response.error);
        List<FullAccountDetails> fullAccountDetailsList = response.result;
        Assert.assertNotNull(fullAccountDetailsList);
        Assert.assertEquals(2, fullAccountDetailsList.size());
        Assert.assertNotNull(fullAccountDetailsList.get(0).getAccount());
        Assert.assertEquals("bilthon-1", fullAccountDetailsList.get(0).getAccount().name);
    }
}
