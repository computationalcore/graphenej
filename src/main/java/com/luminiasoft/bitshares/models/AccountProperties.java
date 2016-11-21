package com.luminiasoft.bitshares.models;

/**
 * Created by nelson on 11/15/16.
 */
public class AccountProperties {
    public String id;
    public String membership_expiration_date;
    public String registrar;
    public String referrer;
    public String lifetime_referrer;
    public long network_fee_percentage;
    public long lifetime_referrer_fee_percentage;
    public long referrer_rewards_percentage;
    public String name;
    public User owner;
    public User active;
    public Options options;
    public String statistics;
    public String[] whitelisting_accounts;
    public String[] blacklisting_accounts;
    public String[] whitelisted_accounts;
    public String[] blacklisted_accounts;
    public Object[] owner_special_authority;
    public Object[] active_special_authority;
    public long top_n_control_flags;

    class User {
        public long weight_threshold;
        public String[] account_auths; //TODO: Check this type
        public String[][] key_auths; //TODO: Check how to deserialize this
        public String[] address_auths;
    }

    class Options {
        public String memo_key;
        public String voting_account;
        public long num_witness;
        public long num_committee;
        public String[] votes; //TODO: Check this type
        public String[] extensions; //TODO: Check this type
    }
}
