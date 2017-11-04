package cy.agorise.graphenej.models;

import cy.agorise.graphenej.AccountOptions;
import cy.agorise.graphenej.Authority;

/**
 * Created by nelson on 11/15/16.
 *
 * Details of Dynamic Account specs can be found at
 * https://bitshares.org/technology/dynamic-account-permissions/
 *
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
    public Authority owner;
    public Authority active;
    public AccountOptions options;
    public String statistics;
    public String[] whitelisting_accounts;
    public String[] blacklisting_accounts;
    public String[] whitelisted_accounts;
    public String[] blacklisted_accounts;
    public Object[] owner_special_authority;
    public Object[] active_special_authority;
    public long top_n_control_flags;
}
