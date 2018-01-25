package cy.agorise.graphenej.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.Util;

/**
 * Class used to deserialize the 'result' field returned by the full node after making a call
 * to the 'get_dynamic_global_properties' RPC.
 */
public class DynamicGlobalProperties extends GrapheneObject implements Serializable {
    public static final String KEY_HEAD_BLOCK_NUMBER = "head_block_number";
    public static final String KEY_HEAD_BLOCK_ID ="head_block_id";
    public static final String KEY_TIME = "time";
    public static final String KEY_CURRENT_WITNESS = "current_witness";
    public static final String KEY_NEXT_MAINTENANCE_TIME = "next_maintenance_time";
    public static final String KEY_LAST_BUDGET_TIME = "last_budget_time";
    public static final String KEY_WITNESS_BUDGET = "witness_budget";
    public static final String KEY_ACCOUNTS_REGISTERED_THIS_INTERVAL = "accounts_registered_this_interval";
    public static final String KEY_RECENTLY_MISSED_COUNT = "recently_missed_count";
    public static final String KEY_CURRENT_ASLOT = "current_aslot";
    public static final String KEY_RECENT_SLOTS_FILLED = "recent_slots_filled";
    public static final String KEY_DYNAMIC_FLAGS = "dynamic_flags";
    public static final String KEY_LAST_IRREVERSIBLE_BLOCK_NUM = "last_irreversible_block_num";

    public long head_block_number;
    public String head_block_id;
    public Date time;
    public String current_witness;
    public Date next_maintenance_time;
    public String last_budget_time;
    public long witness_budget;
    public long accounts_registered_this_interval;
    public long recently_missed_count;
    public long current_aslot;
    public String recent_slots_filled;
    public int dynamic_flags;
    public long last_irreversible_block_num;

    public DynamicGlobalProperties(String id) {
        super(id);
    }

    /**
     * Class that will parse the JSON element containing the dynamic global properties object and
     * return an instance of the {@link DynamicGlobalProperties} class.
     */
    public static class DynamicGlobalPropertiesDeserializer implements JsonDeserializer<DynamicGlobalProperties> {

        @Override
        public DynamicGlobalProperties deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Creating an instance of the DynamicGlobalProperties
            DynamicGlobalProperties dynamicGlobal = new DynamicGlobalProperties(jsonElement.getAsJsonObject().get(KEY_ID).getAsString());

            // Start to fill in the parsed details
            dynamicGlobal.head_block_number = jsonObject.get(DynamicGlobalProperties.KEY_HEAD_BLOCK_NUMBER).getAsLong();
            dynamicGlobal.head_block_id = jsonObject.get(DynamicGlobalProperties.KEY_HEAD_BLOCK_ID).getAsString();

            SimpleDateFormat dateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                dynamicGlobal.time = dateFormat.parse(jsonObject.get(DynamicGlobalProperties.KEY_TIME).getAsString());
            } catch (ParseException e) {
                System.out.println("ParseException. Msg: "+e.getMessage());
            }

            try {
                dynamicGlobal.next_maintenance_time = dateFormat.parse(jsonObject.get(DynamicGlobalProperties.KEY_NEXT_MAINTENANCE_TIME).getAsString());
            } catch (ParseException e) {
                System.out.println("ParseException. Msg: "+e.getMessage());
            }

            dynamicGlobal.current_witness = jsonObject.get(DynamicGlobalProperties.KEY_CURRENT_WITNESS).getAsString();
            dynamicGlobal.last_budget_time = jsonObject.get(DynamicGlobalProperties.KEY_LAST_BUDGET_TIME).getAsString();
            dynamicGlobal.witness_budget = jsonObject.get(DynamicGlobalProperties.KEY_WITNESS_BUDGET).getAsLong();
            dynamicGlobal.accounts_registered_this_interval = jsonObject.get(DynamicGlobalProperties.KEY_ACCOUNTS_REGISTERED_THIS_INTERVAL).getAsLong();
            dynamicGlobal.recently_missed_count = jsonObject.get(DynamicGlobalProperties.KEY_RECENTLY_MISSED_COUNT).getAsLong();
            dynamicGlobal.current_aslot = jsonObject.get(DynamicGlobalProperties.KEY_CURRENT_ASLOT).getAsLong();
            dynamicGlobal.recent_slots_filled = jsonObject.get(DynamicGlobalProperties.KEY_RECENT_SLOTS_FILLED).getAsString();
            dynamicGlobal.dynamic_flags = jsonObject.get(DynamicGlobalProperties.KEY_DYNAMIC_FLAGS).getAsInt();
            dynamicGlobal.last_irreversible_block_num = jsonObject.get(DynamicGlobalProperties.KEY_LAST_IRREVERSIBLE_BLOCK_NUM).getAsLong();
            return dynamicGlobal;
        }
    }
}