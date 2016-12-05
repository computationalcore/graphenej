package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luminiasoft.bitshares.errors.MalformedAddressException;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by nelson on 11/30/16.
 */
public class Authority implements JsonSerializable, ByteSerializable {
    public static final String KEY_ACCOUNT_AUTHS = "account_auths";
    public static final String KEY_KEY_AUTHS = "key_auths";
    public static final String KEY_WEIGHT_THRESHOLD = "weight_threshold";
    public static final String KEY_EXTENSIONS = "extensions";

    private long weight_threshold;
    private HashMap<UserAccount, Integer> account_auths;
    private HashMap<PublicKey, Integer> key_auths;
    private Extensions extensions;

    public Authority(long weight_threshold, HashMap<String, Integer> keyAuths) throws MalformedAddressException {
        this.weight_threshold = weight_threshold;
        key_auths = new HashMap<PublicKey, Integer>();
        for(String key : keyAuths.keySet()){
            Address address = new Address(key);
            key_auths.put(address.getPublicKey(), keyAuths.get(key));
        }
        account_auths = new HashMap<UserAccount, Integer>();
        extensions = new Extensions();
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

        // Weight threshold
        byteArray.addAll(Bytes.asList(Util.revertInteger(new Integer((int) weight_threshold))));

        // Number of account authorities
        byteArray.add((byte) account_auths.size());

        //TODO: Add account authorities

        // Number of key authorities
        byteArray.add((byte) key_auths.size());

        for(PublicKey publicKey : key_auths.keySet()){
            byteArray.addAll(Bytes.asList(publicKey.toBytes()));
            byteArray.addAll(Bytes.asList(Util.revertShort(key_auths.get(publicKey).shortValue())));
        }

        // Adding number of extensions
        byteArray.add((byte) extensions.size());

        return Bytes.toArray(byteArray);
    }
}
