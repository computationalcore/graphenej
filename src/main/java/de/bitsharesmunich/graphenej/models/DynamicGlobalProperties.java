package de.bitsharesmunich.graphenej.models;

import java.io.Serializable;

import de.bitsharesmunich.graphenej.GrapheneObject;

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
    public String time;
    public String current_witness;
    public String next_maintenance_time;
    public String last_budget_time;
    public long witness_budget;
    public long accounts_registered_this_interval;
    public long recently_missed_count;
    public long current_aslot;
    public String recent_slots_filled;
    public long dynamic_flags;
    public long last_irreversible_block_num;

    public DynamicGlobalProperties(String id) {
        super(id);
    }
}