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
import java.util.List;

import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.interfaces.GrapheneSerializable;

/**
 * Created by nelson on 12/5/16.
 */
public class AccountOptions implements GrapheneSerializable {
    public static final String KEY_MEMO_KEY = "memo_key";
    public static final String KEY_NUM_COMMITTEE = "num_committee";
    public static final String KEY_NUM_WITNESS = "num_witness";
    public static final String KEY_VOTES = "votes";
    public static final String KEY_VOTING_ACCOUNT = "voting_account";
    public static final String KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS;

    private PublicKey memo_key;
    private UserAccount voting_account;
    private int num_witness;
    private int num_comittee;
    private Vote[] votes;
    private Extensions extensions;

    /**
     * Constructor used to instantiate only the following attributes:
     * <ul>
     *     <li>voting_account</li>
     *     <li>votes</li>
     *     <li>extensions</li>
     * </ul>
     */
    public AccountOptions(){
        voting_account = new UserAccount(UserAccount.PROXY_TO_SELF);
        this.votes = new Vote[0];
        this.extensions = new Extensions();
    }

    /**
     * Constructor used to instantiate only the following attributes:
     * <ul>
     *     <li>voting_account</li>
     *     <li>votes</li>
     *     <li>memo_key</li>
     *     <li>extensions</li>
     * </ul>
     */
    public AccountOptions(PublicKey memoKey){
        this();
        this.memo_key = memoKey;
    }

    /**
     * Constructor that can be used to instantiate a version of the AccountOptions object
     * with a null reference in the 'voting_account' attribute. This can be used to prevent
     * a circular dependency situation when de-serializing the UserAccount instance.
     *
     * @param memoKey          Memo public key used by this account
     * @param includeAccount   Whether or not to instantiate an UserAccount
     */
    public AccountOptions(PublicKey memoKey, boolean includeAccount){
        if(includeAccount){
            voting_account = new UserAccount(UserAccount.PROXY_TO_SELF);
        }
        this.memo_key = memoKey;
        this.votes = new Vote[0];
        this.extensions = new Extensions();
    }

    //TODO: Implement constructor that takes a Vote array.

    public PublicKey getMemoKey() {
        return memo_key;
    }

    public void setMemoKey(PublicKey memo_key) {
        this.memo_key = memo_key;
    }

    public UserAccount getVotingAccount() {
        return voting_account;
    }

    public void setVotingAccount(UserAccount voting_account) {
        this.voting_account = voting_account;
    }

    public int getNumWitness() {
        return num_witness;
    }

    public void setNumWitness(int num_witness) {
        this.num_witness = num_witness;
    }

    public int getNumComittee() {
        return num_comittee;
    }

    public void setNum_comittee(int num_comittee) {
        this.num_comittee = num_comittee;
    }

    public Vote[] getVotes() {
        return votes;
    }

    public void setVotes(Vote[] votes) {
        this.votes = votes;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> byteArray = new ArrayList<Byte>();

        if(memo_key != null){
            // Adding byte to indicate that there is memo data
            byteArray.add((byte) 1);

            // Adding memo key
            byteArray.addAll(Bytes.asList(memo_key.toBytes()));

            // Adding voting account
            byteArray.addAll(Bytes.asList(voting_account.toBytes()));

            // Adding num_witness
            byteArray.addAll(Bytes.asList(Util.revertShort(Short.valueOf((short) num_witness))));

            // Adding num_committee
            byteArray.addAll(Bytes.asList(Util.revertShort(Short.valueOf((short) num_comittee))));

            // Vote's array length
            byteArray.add((byte) votes.length);

            for(Vote vote : votes){
                //TODO: Check this serialization
                byteArray.addAll(Bytes.asList(vote.toBytes()));
            }

            // Account options's extensions
            byteArray.addAll(Bytes.asList(extensions.toBytes()));
        }else{
            byteArray.add((byte) 0);
        }
        return Bytes.toArray(byteArray);
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject options = new JsonObject();
        options.addProperty(KEY_MEMO_KEY, new Address(memo_key.getKey()).toString());
        options.addProperty(KEY_NUM_COMMITTEE, num_comittee);
        options.addProperty(KEY_NUM_WITNESS, num_witness);
        options.addProperty(KEY_VOTING_ACCOUNT, voting_account.getObjectId());
        JsonArray votesArray = new JsonArray();
        for(Vote vote : votes){
            //TODO: Add votes representation
        }
        options.add(KEY_VOTES, votesArray);
        options.add(KEY_EXTENSIONS, extensions.toJsonObject());
        return options;
    }

    /**
     * Custom deserializer used while parsing the 'get_account_by_name' API call response.
     * TODO: Implement all other details besides the key
     */
    public static class AccountOptionsDeserializer implements JsonDeserializer<AccountOptions> {

        boolean mIncludeUserAccount;

        public AccountOptionsDeserializer(){
            this.mIncludeUserAccount = true;
        }

        public AccountOptionsDeserializer(boolean includeUserAccount){
            this.mIncludeUserAccount = includeUserAccount;
        }

        @Override
        public AccountOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject baseObject = json.getAsJsonObject();
            AccountOptions options;
            try {
                Address address = new Address(baseObject.get(KEY_MEMO_KEY).getAsString());
                options = new AccountOptions(address.getPublicKey(), mIncludeUserAccount);
            } catch (MalformedAddressException e) {
                System.out.println("MalformedAddressException. Msg: "+e.getMessage());
                options = new AccountOptions();
            }
            return options;
        }
    }
}
