package cy.agorise.graphenej;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.interfaces.GrapheneSerializable;

/**
 * Class used to represent the weighted set of keys and accounts that must approve operations.
 *
 * {@see <a href="https://bitshares.org/doxygen/structgraphene_1_1chain_1_1authority.html">Authority</a>}
 */
public class Authority implements GrapheneSerializable {
    public static final String KEY_ACCOUNT_AUTHS = "account_auths";
    public static final String KEY_KEY_AUTHS = "key_auths";
    public static final String KEY_WEIGHT_THRESHOLD = "weight_threshold";
    public static final String KEY_EXTENSIONS = "extensions";

    private long weight_threshold;
    private HashMap<UserAccount, Long> account_auths;
    private HashMap<PublicKey, Long> key_auths;
    private Extensions extensions;

    public Authority(){
        this.weight_threshold = 1;
        this.account_auths = new HashMap<UserAccount, Long>();
        this.key_auths = new HashMap<PublicKey, Long>();
        extensions = new Extensions();
    }

    /**
     * Constructor for the authority class that takes every possible detail.
     * @param weight_threshold: The total weight threshold
     * @param keyAuths: Map of key to weights relationships. Can be null.
     * @param accountAuths: Map of account to weights relationships. Can be null.
     * @throws MalformedAddressException
     */
    public Authority(long weight_threshold, HashMap<PublicKey, Long> keyAuths, HashMap<UserAccount, Long> accountAuths) {
        this();
        this.weight_threshold = weight_threshold;
        if(keyAuths != null)
            this.key_auths = keyAuths;
        else
            this.key_auths = new HashMap<>();
        if(accountAuths != null)
            this.account_auths = accountAuths;
        else
            this.account_auths = new HashMap<>();
    }

    public long getWeightThreshold() {
        return weight_threshold;
    }

    public void setWeightThreshold(long weight_threshold) {
        this.weight_threshold = weight_threshold;
    }

    public void setKeyAuthorities(HashMap<Address, Long> keyAuths){
        if(keyAuths != null){
            for(Address address : keyAuths.keySet()){
                key_auths.put(address.getPublicKey(), keyAuths.get(address));
            }
        }
    }

    public void setAccountAuthorities(HashMap<UserAccount, Long> accountAuthorities){
        this.account_auths = accountAuthorities;
    }

    /**
     * @return: Returns a list of public keys linked to this authority
     */
    public List<PublicKey> getKeyAuthList(){
        ArrayList<PublicKey> keys = new ArrayList<>();
        for(PublicKey pk : key_auths.keySet()){
            keys.add(pk);
        }
        return keys;
    }

    /**
     * @return: Returns a list of accounts linked to this authority
     */
    public List<UserAccount> getAccountAuthList(){
        ArrayList<UserAccount> accounts = new ArrayList<>();
        for(UserAccount account : account_auths.keySet()){
            accounts.add(account);
        }
        return accounts;
    }

    public HashMap<PublicKey, Long> getKeyAuths(){
        return this.key_auths;
    }

    public HashMap<UserAccount, Long> getAccountAuths(){
        return this.account_auths;
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject authority = new JsonObject();
        authority.addProperty(KEY_WEIGHT_THRESHOLD, weight_threshold);
        JsonArray keyAuthArray = new JsonArray();
        JsonArray accountAuthArray = new JsonArray();

        for(PublicKey publicKey : key_auths.keySet()){
            JsonArray subArray = new JsonArray();
            Address address = new Address(publicKey.getKey());
            subArray.add(address.toString());
            subArray.add(key_auths.get(publicKey));
            keyAuthArray.add(subArray);
        }

        for(UserAccount key : account_auths.keySet()){
            JsonArray subArray = new JsonArray();
            subArray.add(key.toString());
            subArray.add(key_auths.get(key));
            accountAuthArray.add(subArray);
        }
        authority.add(KEY_KEY_AUTHS, keyAuthArray);
        authority.add(KEY_ACCOUNT_AUTHS, accountAuthArray);
        authority.add(KEY_EXTENSIONS, extensions.toJsonObject());
        return authority;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> byteArray = new ArrayList<Byte>();
        // Adding number of authorities
        byteArray.add(Byte.valueOf((byte) (account_auths.size() + key_auths.size())));

        // If the authority is not empty of references, we serialize its contents
        // otherwise its only contribution will be a zero byte
        if(account_auths.size() + key_auths.size() > 0){
            // Weight threshold
            byteArray.addAll(Bytes.asList(Util.revertInteger(new Integer((int) weight_threshold))));

            // Number of account authorities
            byteArray.add((byte) account_auths.size());

            //TODO: Check the account authorities serialization
            // Serializing individual accounts and their corresponding weights
            for(UserAccount account : account_auths.keySet()){
                byteArray.addAll(Bytes.asList(account.toBytes()));
                byteArray.addAll(Bytes.asList(Util.revertShort(account_auths.get(account).shortValue())));
            }

            // Number of key authorities
            byteArray.add((byte) key_auths.size());

            // Serializing individual keys and their corresponding weights
            for(PublicKey publicKey : key_auths.keySet()){
                byteArray.addAll(Bytes.asList(publicKey.toBytes()));
                byteArray.addAll(Bytes.asList(Util.revertShort(key_auths.get(publicKey).shortValue())));
            }

            // Adding number of extensions
            byteArray.add((byte) extensions.size());
        }
        return Bytes.toArray(byteArray);
    }

    @Override
    public boolean equals(Object obj) {
        Authority authority = (Authority) obj;
        HashMap<PublicKey, Long> keyAuths = authority.getKeyAuths();
        HashMap<UserAccount, Long> accountAuths = authority.getAccountAuths();
        System.out.println("key auths match: "+this.key_auths.equals(keyAuths));
        System.out.println("account auths match: "+this.account_auths.equals(accountAuths));
        System.out.println("weight threshold matches: "+(this.weight_threshold == authority.weight_threshold));
        return this.key_auths.equals(keyAuths) &&
                this.account_auths.equals(accountAuths) &&
                this.weight_threshold == authority.weight_threshold;
    }

    /**
     * Custom deserializer used while parsing the 'get_account_by_name' API call response.
     *
     * This will deserialize an account authority in the form:
     *
     * {
     *   "weight_threshold": 1,
     *   "account_auths": [],
     *   "key_auths": [["BTS6yoiaoC4p23n31AV4GnMy5QDh5yUQEUmU4PmNxRQPGg7jjPkBq",1]],
     *   "address_auths": []
     * }
     */
    public static class AuthorityDeserializer implements JsonDeserializer<Authority> {

        @Override
        public Authority deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject baseObject = json.getAsJsonObject();
            long weightThreshold = baseObject.get(KEY_WEIGHT_THRESHOLD).getAsLong();
            JsonArray keyAuthArray = baseObject.getAsJsonArray(KEY_KEY_AUTHS);
            JsonArray accountAuthArray = baseObject.getAsJsonArray(KEY_ACCOUNT_AUTHS);
            HashMap<PublicKey, Long> keyAuthMap = new HashMap<>();
            HashMap<UserAccount, Long> accountAuthMap = new HashMap<>();
            for(int i = 0; i < keyAuthArray.size(); i++){
                JsonArray subArray = keyAuthArray.get(i).getAsJsonArray();
                String addr = subArray.get(0).getAsString();
                long weight = subArray.get(1).getAsLong();
                try {
                    keyAuthMap.put(new Address(addr).getPublicKey(), weight);
                } catch (MalformedAddressException e) {
                    System.out.println("MalformedAddressException. Msg: "+e.getMessage());
                }
            }
            for(int i = 0; i < accountAuthArray.size(); i++){
                JsonArray subArray = accountAuthArray.get(i).getAsJsonArray();
                String userId = subArray.get(0).getAsString();
                long weight = subArray.get(1).getAsLong();
                UserAccount userAccount = new UserAccount(userId);
                accountAuthMap.put(userAccount, weight);
            }
            return new Authority(weightThreshold, keyAuthMap, accountAuthMap);
        }
    }
}