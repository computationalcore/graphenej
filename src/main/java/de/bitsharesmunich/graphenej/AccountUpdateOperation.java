package de.bitsharesmunich.graphenej;

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
    public static final String KEY_NEW_OPTIONS = "new_options";
    public static final String KEY_EXTENSIONS = "extensions";

    private AssetAmount fee;
    private UserAccount account;
    private Optional<Authority> owner;
    private Optional<Authority> active;
    private Optional<AccountOptions> new_options;
    private Extensions extensions;

    /**
     * Account update operation constructor.
     * @param account User account to update. Can't be null.
     * @param owner Owner authority to set. Can be null.
     * @param active Active authority to set. Can be null.
     * @param options Active authority to set. Can be null.
     * @param fee The fee to pay. Can be null.
     */
    public AccountUpdateOperation(UserAccount account, Authority owner, Authority active, AccountOptions options, AssetAmount fee){
        super(OperationType.account_update_operation);
        this.fee = fee;
        this.account = account;
        this.owner = new Optional<>(owner);
        this.active = new Optional<>(active);
        this.new_options = new Optional<>(options);
        extensions = new Extensions();
    }

    public AccountUpdateOperation(UserAccount account, Authority owner, Authority active, AccountOptions options){
        this(account, owner, active, options, new AssetAmount(UnsignedLong.valueOf(0), new Asset("1.3.0")));
    }

    @Override
    public void setFee(AssetAmount fee){
        this.fee = fee;
    }

    public void setOwner(Authority owner){
        this.owner = new Optional<>(owner);
    }

    public void setActive(Authority active){
        this.active = new Optional<>(active);
    }

    public void setAccountOptions(AccountOptions options){
        this.new_options = new Optional<>(options);
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
        if(owner.isSet())
            accountUpdate.add(KEY_OWNER, owner.toJsonObject());
        if(active.isSet())
            accountUpdate.add(KEY_ACTIVE, active.toJsonObject());
        if(new_options.isSet())
            accountUpdate.add(KEY_NEW_OPTIONS, new_options.toJsonObject());
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
        byte[] newOptionsBytes = new_options.toBytes();
        byte[] extensionBytes = extensions.toBytes();
        return Bytes.concat(feeBytes, accountBytes, ownerBytes, activeBytes, newOptionsBytes, extensionBytes);
    }
}
