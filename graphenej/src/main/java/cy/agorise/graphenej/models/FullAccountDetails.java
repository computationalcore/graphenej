package cy.agorise.graphenej.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Model class used in the de-serialization of the response to the 'get_full_accounts' API call.
 * @see cy.agorise.graphenej.api.calls.GetFullAccounts
 */
public class FullAccountDetails {
    private AccountProperties account;
    private Statistics statistics;

    public FullAccountDetails(AccountProperties properties, Statistics statistics){
        this.account = properties;
        this.statistics = statistics;
    }

    public AccountProperties getAccount() {
        return account;
    }

    public void setAccount(AccountProperties account) {
        this.account = account;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public static class Statistics {
        public String id;
        public String owner;
        public String name;
        public String most_recent_op;
        public long total_ops;
        public long removed_ops;
        public long total_core_in_orders;
        public String core_in_balance;
        public boolean has_cashback_vb;
        public boolean is_voting;
        public long lifetime_fees_paid;
        public long pending_fees;
        public long pending_vested_fees;
    }

    public static class FullAccountDeserializer implements JsonDeserializer<FullAccountDetails> {

        @Override
        public FullAccountDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = (JsonArray) json;
            JsonObject jsonObject = (JsonObject) array.get(1);
            AccountProperties properties = context.deserialize(jsonObject.get("account"), AccountProperties.class);
            Statistics statistics = context.deserialize(jsonObject.get("statistics"), Statistics.class);
            return new FullAccountDetails(properties, statistics);
        }
    }
}
