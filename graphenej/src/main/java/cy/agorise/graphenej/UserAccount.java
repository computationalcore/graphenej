package cy.agorise.graphenej;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cy.agorise.graphenej.interfaces.ByteSerializable;
import cy.agorise.graphenej.interfaces.JsonSerializable;

/**
 * Class that represents a graphene user account.
 */
public class UserAccount extends GrapheneObject implements ByteSerializable, JsonSerializable {

    public static final String PROXY_TO_SELF = "1.2.5";
    public static final String KEY_MEMBERSHIP_EXPIRATION_DATE = "membership_expiration_date";
    public static final String KEY_REGISTRAR = "registrar";
    public static final String KEY_REFERRER = "referrer";
    public static final String KEY_LIFETIME_REFERRER = "lifetime_referrer";
    public static final String KEY_NETWORK_FEE_PERCENTAGE = "network_fee_percentage";
    public static final String KEY_LIFETIME_REFERRER_FEE_PERCENTAGE = "lifetime_referrer_fee_percentage";
    public static final String KEY_REFERRER_REWARD_PERCENTAGE = "referrer_rewards_percentage";
    public static final String KEY_NAME = "name";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_OPTIONS = "options";
    public static final String KEY_STATISTICS = "statistics";
    public static final String KEY_WHITELISTING_ACCOUNTS = "whitelisting_accounts";
    public static final String KEY_BLACKLISTING_ACCOUNTS = "blacklisting_accounts";
    public static final String KEY_WHITELISTED_ACCOUNTS = "whitelisted_accounts";
    public static final String KEY_BLACKLISTED_ACCOUNTS = "blacklisted_accounts";
    public static final String KEY_OWNER_SPECIAL_AUTHORITY = "owner_special_authority";
    public static final String KEY_ACTIVE_SPECIAL_AUTHORITY = "active_special_authority";
    public static final String KEY_N_CONTROL_FLAGS = "top_n_control_flags";
    public static final String LIFETIME_EXPIRATION_DATE = "1969-12-31T23:59:59";

    @Expose
    private String name;

    @Expose
    private Authority owner;

    @Expose
    private Authority active;

    @Expose
    private AccountOptions options;

    @Expose
    private String statistics;

    @Expose
    private long membershipExpirationDate;

    @Expose
    private String registrar;

    @Expose
    private String referrer;

    @Expose
    private String lifetimeReferrer;

    @Expose
    private long networkFeePercentage;

    @Expose
    private long lifetimeReferrerFeePercentage;

    @Expose
    private long referrerRewardsPercentage;

    private boolean isLifeTime;


    /**
     * Constructor that expects a user account in the string representation.
     * That is in the 1.2.x format.
     * @param id: The string representing the user account.
     */
    public UserAccount(String id) {
        super(id);
    }

    /**
     * Constructor that expects a user account withe the proper graphene object id and an account name.
     * @param id: The string representing the user account.
     * @param name: The name of this user account.
     */
    public UserAccount(String id, String name){
        super(id);
        this.name = name;
    }

    /**
     * Getter for the account name field.
     * @return: The name of this account.
     */
    public String getName() {
        return name;
    }


    /**
     * Setter for the account name field.
     * @param name: The account name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return this.getObjectId().equals(((UserAccount)o).getObjectId());
    }

    @Override
    public int hashCode() {
        return this.getObjectId().hashCode();
    }
    
    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(byteArrayOutputStream);
        try {
            Varint.writeUnsignedVarLong(this.instance, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public String toJsonString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        return null;
    }

    @Override
    public String toString() {
        return this.toJsonString();
    }

    public long getMembershipExpirationDate() {
        return membershipExpirationDate;
    }

    public void setMembershipExpirationDate(long membershipExpirationDate) {
        this.membershipExpirationDate = membershipExpirationDate;
    }

    public String getRegistrar() {
        return registrar;
    }

    public void setRegistrar(String registrar) {
        this.registrar = registrar;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getLifetimeReferrer() {
        return lifetimeReferrer;
    }

    public void setLifetimeReferrer(String lifetimeReferrer) {
        this.lifetimeReferrer = lifetimeReferrer;
    }

    public long getNetworkFeePercentage() {
        return networkFeePercentage;
    }

    public void setNetworkFeePercentage(long networkFeePercentage) {
        this.networkFeePercentage = networkFeePercentage;
    }

    public long getLifetimeReferrerFeePercentage() {
        return lifetimeReferrerFeePercentage;
    }

    public void setLifetimeReferrerFeePercentage(long lifetimeReferrerFeePercentage) {
        this.lifetimeReferrerFeePercentage = lifetimeReferrerFeePercentage;
    }

    public long getReferrerRewardsPercentage() {
        return referrerRewardsPercentage;
    }

    public void setReferrerRewardsPercentage(long referrerRewardsPercentage) {
        this.referrerRewardsPercentage = referrerRewardsPercentage;
    }

    public Authority getOwner() {
        return owner;
    }

    public void setOwner(Authority owner) {
        this.owner = owner;
    }

    public Authority getActive() {
        return active;
    }

    public void setActive(Authority active) {
        this.active = active;
    }

    public AccountOptions getOptions() {
        return options;
    }

    public void setOptions(AccountOptions options) {
        this.options = options;
    }

    public String getStatistics() {
        return statistics;
    }

    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    public boolean isLifeTime() {
        return isLifeTime;
    }

    public void setLifeTime(boolean lifeTime) {
        isLifeTime = lifeTime;
    }

    /**
     * Deserializer used to build a UserAccount instance from the full JSON-formatted response obtained
     * by the 'get_objects' API call.
     */
    public static class UserAccountFullDeserializer implements JsonDeserializer<UserAccount> {

        @Override
        public UserAccount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonAccount = json.getAsJsonObject();

            SimpleDateFormat dateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);

            // Retrieving and deserializing fields
            String id = jsonAccount.get(KEY_ID).getAsString();
            String name = jsonAccount.get(KEY_NAME).getAsString();
            UserAccount userAccount = new UserAccount(id, name);
            AccountOptions options = context.deserialize(jsonAccount.get(KEY_OPTIONS), AccountOptions.class);
            Authority owner = context.deserialize(jsonAccount.get(KEY_OWNER), Authority.class);
            Authority active = context.deserialize(jsonAccount.get(KEY_ACTIVE), Authority.class);

            // Setting deserialized fields into the created instance
            userAccount.setRegistrar(jsonAccount.get(KEY_REGISTRAR).getAsString());

            // Handling the deserialization and assignation of the membership date, which internally
            // is stored as a long POSIX time value
            try{
                String expirationDate = jsonAccount.get(KEY_MEMBERSHIP_EXPIRATION_DATE).getAsString();
                Date date = dateFormat.parse(expirationDate);
                userAccount.setMembershipExpirationDate(date.getTime());
                userAccount.setLifeTime(expirationDate.equals(LIFETIME_EXPIRATION_DATE));
            } catch (ParseException e) {
                System.out.println("ParseException. Msg: "+e.getMessage());
            }

            // Setting the other fields
            userAccount.setReferrer(jsonAccount.get(KEY_REFERRER).getAsString());
            userAccount.setLifetimeReferrer(jsonAccount.get(KEY_LIFETIME_REFERRER).getAsString());
            userAccount.setNetworkFeePercentage(jsonAccount.get(KEY_NETWORK_FEE_PERCENTAGE).getAsLong());
            userAccount.setLifetimeReferrerFeePercentage(jsonAccount.get(KEY_LIFETIME_REFERRER_FEE_PERCENTAGE).getAsLong());
            userAccount.setReferrerRewardsPercentage(jsonAccount.get(KEY_REFERRER_REWARD_PERCENTAGE).getAsLong());
            userAccount.setOwner(owner);
            userAccount.setActive(active);
            userAccount.setOptions(options);
            userAccount.setStatistics(jsonAccount.get(KEY_STATISTICS).getAsString());
            return userAccount;
        }
    }

    /**
     * Custom deserializer used to deserialize user accounts provided as response from the 'lookup_accounts' api call.
     * This response contains serialized user accounts in the form [[{id1},{name1}][{id1},{name1}]].
     *
     * For instance:
     *  [["bilthon-1","1.2.139205"],["bilthon-2","1.2.139207"],["bilthon-2016","1.2.139262"]]
     *
     * So this class will pick up this data and turn it into a UserAccount object.
     */
    public static class UserAccountDeserializer implements JsonDeserializer<UserAccount> {

        @Override
        public UserAccount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = json.getAsJsonArray();
            String name = array.get(0).getAsString();
            String id = array.get(1).getAsString();
            return new UserAccount(id, name);
        }
    }

    /**
     * Custom deserializer used to deserialize user accounts as provided by the response of the 'get_key_references' api call.
     * This response contains serialized user accounts in the form [["id1","id2"]]
     */
    public static class UserAccountSimpleDeserializer implements JsonDeserializer<UserAccount> {

        @Override
        public UserAccount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String id = json.getAsString();
            return new UserAccount(id);
        }
    }
}
