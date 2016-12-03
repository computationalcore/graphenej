package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonElement;
import com.luminiasoft.bitshares.errors.MalformedAddressException;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by nelson on 11/30/16.
 */
public class Authority implements JsonSerializable, ByteSerializable {
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
        return null;
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
