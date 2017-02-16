package de.bitsharesmunich.graphenej;

import com.google.gson.*;
import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Class tha represents a graphene user account.
 * Created by nelson on 11/8/16.
 */
public class UserAccount extends GrapheneObject implements ByteSerializable, JsonSerializable {

    public static final String PROXY_TO_SELF = "1.2.5";

    private String accountName;

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
        this.accountName = name;
    }

    /**
     * Getter for the account name field.
     * @return: The name of this account.
     */
    public String getAccountName() {
        return accountName;
    }


    /**
     * Setter for the account name field.
     * @param accountName: The account name.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
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
        return this.getObjectId();
    }

    @Override
    public JsonObject toJsonObject() {
        return null;
    }

    @Override
    public String toString() {
        return this.toJsonString();
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
