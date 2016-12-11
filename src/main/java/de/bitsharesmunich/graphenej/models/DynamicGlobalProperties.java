package de.bitsharesmunich.graphenej.models;

/**
 * Class used to deserialize the 'result' field returned by the full node after making a call
 * to the 'get_dynamic_global_properties' RPC.
 */
public class DynamicGlobalProperties {
    public String id;
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
}
