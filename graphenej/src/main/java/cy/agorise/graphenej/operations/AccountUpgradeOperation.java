package cy.agorise.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.OperationType;
import cy.agorise.graphenej.UserAccount;

/**
 * Created by henry on 19/5/2018.
 */

public class AccountUpgradeOperation extends BaseOperation {

    private static final String KEY_ACCOUNT = "account_to_upgrade";
    private static final String KEY_UPGRADE = "upgrade_to_lifetime_member";

    private AssetAmount fee;
    private UserAccount accountToUpgrade;
    private boolean upgradeToLifeTimeMember;

    public AccountUpgradeOperation(UserAccount accountToUpgrade, boolean upgradeToLifeTimeMember) {
        super(OperationType.ACCOUNT_UPGRADE_OPERATION);
        this.accountToUpgrade = accountToUpgrade;
        this.upgradeToLifeTimeMember = upgradeToLifeTimeMember;
    }

    public AccountUpgradeOperation(UserAccount accountToUpgrade, boolean upgradeToLifeTimeMember, AssetAmount fee) {
        super(OperationType.ACCOUNT_UPGRADE_OPERATION);
        this.accountToUpgrade = accountToUpgrade;
        this.upgradeToLifeTimeMember = upgradeToLifeTimeMember;
        this.fee = fee;
    }

    public AssetAmount getFee() {
        return fee;
    }

    public UserAccount getAccountToUpgrade() {
        return accountToUpgrade;
    }

    public void setAccountToUpgrade(UserAccount accountToUpgrade) {
        this.accountToUpgrade = accountToUpgrade;
    }

    public boolean isUpgradeToLifeTimeMember() {
        return upgradeToLifeTimeMember;
    }

    public void setUpgradeToLifeTimeMember(boolean upgradeToLifeTimeMember) {
        this.upgradeToLifeTimeMember = upgradeToLifeTimeMember;
    }

    @Override
    public void setFee(AssetAmount assetAmount) {
        this.fee = assetAmount;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = fee.toBytes();
        byte[] accountBytes = accountToUpgrade.toBytes();
        byte[] upgradeToLifeTimeMemberBytes = this.upgradeToLifeTimeMember ? new byte[]{ 0x1 } : new byte[]{ 0x0 };
        byte[] extensions = this.extensions.toBytes();
        return Bytes.concat(feeBytes, accountBytes, upgradeToLifeTimeMemberBytes, extensions);
    }

    @Override
    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AccountUpgradeOperation.class, new AccountUpgradeSerializer());
        return gsonBuilder.create().toJson(this);
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = new JsonArray();
        array.add(this.getId());
        JsonObject jsonObject = new JsonObject();
        if(fee != null)
            jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.addProperty(KEY_ACCOUNT, accountToUpgrade.getObjectId());
        jsonObject.addProperty(KEY_UPGRADE, this.upgradeToLifeTimeMember ? "true" : "false");
        jsonObject.add(KEY_EXTENSIONS, new JsonArray());
        array.add(jsonObject);
        return array;
    }

    public static class AccountUpgradeSerializer implements JsonSerializer<AccountUpgradeOperation> {

        @Override
        public JsonElement serialize(AccountUpgradeOperation accountUpgrade, Type type, JsonSerializationContext jsonSerializationContext) {
            return accountUpgrade.toJsonObject();
        }
    }


    public static class AccountUpgradeDeserializer implements JsonDeserializer<AccountUpgradeOperation> {

        @Override
        public AccountUpgradeOperation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(json.isJsonArray()){
                // This block is used just to check if we are in the first step of the deserialization
                // when we are dealing with an array.
                JsonArray serializedAccountUpgrade = json.getAsJsonArray();
                if(serializedAccountUpgrade.get(0).getAsInt() != OperationType.ACCOUNT_UPGRADE_OPERATION.ordinal()){
                    // If the operation type does not correspond to a transfer operation, we return null
                    return null;
                }else{
                    // Calling itself recursively, this is only done once, so there will be no problems.
                    return context.deserialize(serializedAccountUpgrade.get(1), AccountUpgradeOperation.class);
                }
            }else{
                JsonObject jsonObject = json.getAsJsonObject();

                // Deserializing AssetAmount objects
                AssetAmount fee = context.deserialize(jsonObject.get(KEY_FEE), AssetAmount.class);

                // Deserializing UserAccount objects
                UserAccount accountToUpgrade = new UserAccount(jsonObject.get(KEY_ACCOUNT).getAsString());

                boolean upgradeToLifeTime = jsonObject.get(KEY_UPGRADE).getAsBoolean();
                AccountUpgradeOperation upgrade = new AccountUpgradeOperation(accountToUpgrade, upgradeToLifeTime, fee);

                return upgrade;
            }
        }
    }


}

