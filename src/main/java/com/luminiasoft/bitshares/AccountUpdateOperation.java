package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class used to encapsulate operations related to the account_update_operation.
 */
public class AccountUpdateOperation extends BaseOperation {
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_FEE = "fee";
    public static final String KEY_EXTENSIONS = "extensions";

    private UserAccount account;
    private AssetAmount fee;
    private Authority owner;
    private Authority active;
    private Extensions extensions;

    public AccountUpdateOperation(UserAccount account, Authority owner, Authority active, AssetAmount fee){
        super(OperationType.account_update_operation);
        this.account = account;
        this.owner = owner;
        this.active = active;
        this.fee = fee;
        extensions = new Extensions();
    }

    public AccountUpdateOperation(UserAccount account, Authority owner, Authority active){
        this(account, owner, active, new AssetAmount(UnsignedLong.valueOf(0), new Asset("1.3.0")));
    }

    public void setFee(AssetAmount fee){
        this.fee = fee;
    }

    @Override
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = new JsonArray();
        array.add(this.getId());

        JsonObject accountUpdate = new JsonObject();
        accountUpdate.add(KEY_FEE, fee.toJsonObject());
        accountUpdate.addProperty(KEY_ACCOUNT, account.toJsonString());
        accountUpdate.add(KEY_OWNER, owner.toJsonObject());
        accountUpdate.add(KEY_ACTIVE, active.toJsonObject());
        accountUpdate.add(KEY_EXTENSIONS, extensions.toJsonObject());
        array.add(accountUpdate);
        return array;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = fee.toBytes();
        byte[] accountBytes = account.toBytes();
        byte[] ownerBytes = owner.toBytes();
        byte[] activeBytes = active.toBytes();
        byte[] extensionBytes = extensions.toBytes();
        return Bytes.concat(feeBytes, accountBytes, ownerBytes, activeBytes, extensionBytes);
    }
}
