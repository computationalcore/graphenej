package de.bitsharesmunich.graphenej.models;

import java.io.Serializable;

import de.bitsharesmunich.graphenej.GrapheneObject;

/**
 * Created by nelson on 1/12/17.
 */

public class AccountBalanceUpdate extends GrapheneObject implements Serializable {
    public static final String KEY_OWNER = "owner";
    public static final String KEY_ASSET_TYPE = "asset_type";
    public static final String KEY_BALANCE = "balance";

    public String owner;
    public String asset_type;
    public long balance;

    public AccountBalanceUpdate(String id) {
        super(id);
    }
}
