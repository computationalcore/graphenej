package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luminiasoft.bitshares.errors.MalformedAddressException;
import com.luminiasoft.bitshares.interfaces.GrapheneSerializable;

import java.util.*;

/**
 * Created by nelson on 11/30/16.
 */
public class Authority implements GrapheneSerializable {
    public static final String KEY_ACCOUNT_AUTHS = "account_auths";
    public static final String KEY_KEY_AUTHS = "key_auths";
    public static final String KEY_WEIGHT_THRESHOLD = "weight_threshold";
    public static final String KEY_EXTENSIONS = "extensions";

    private long weight_threshold;
    private HashMap<UserAccount, Integer> account_auths;
    private HashMap<PublicKey, Integer> key_auths;
    private Extensions extensions;

    public Authority(){
        this.weight_threshold = 1;
        this.account_auths = new HashMap<UserAccount, Integer>();
        this.key_auths = new HashMap<PublicKey, Integer>();
        extensions = new Extensions();
    }

    /**
     * Constructor for the authority class that takes every possible detail.
     * @param weight_threshold: The total weight threshold
     * @param keyAuths: Map of key to weights relationships. Can be null.
     * @param accountAuths: Map of account to weights relationships. Can be null.
     * @throws MalformedAddressException
     */
    public Authority(long weight_threshold, HashMap<PublicKey, Integer> keyAuths, HashMap<UserAccount, Integer> accountAuths) {
        this();
        this.weight_threshold = weight_threshold;
        if(keyAuths != null)
            this.key_auths = keyAuths;
        if(accountAuths != null)
            this.account_auths = accountAuths;
    }

    public void setKeyAuthorities(HashMap<Address, Integer> keyAuths){
        if(keyAuths != null){
            for(Address address : keyAuths.keySet()){
                key_auths.put(address.getPublicKey(), keyAuths.get(address));
            }
        }
    }

    public void setAccountAuthorities(HashMap<UserAccount, Integer> accountAuthorities){
        this.account_auths = accountAuthorities;
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
}