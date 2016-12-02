package com.luminiasoft.bitshares;

import com.google.gson.JsonElement;
import com.luminiasoft.bitshares.errors.MalformedAddressException;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

import java.util.HashMap;

/**
 * Created by nelson on 11/30/16.
 */
public class Authority implements JsonSerializable {
    private long weight_threshold;
    private HashMap<Address, Long> address_auths;
    private HashMap<UserAccount, Long> account_auths;
    private HashMap<PublicKey, Long> key_auths;

    public Authority(HashMap<String, Long> keyAuths) throws MalformedAddressException {
        key_auths = new HashMap<PublicKey, Long>();
        for(String key : keyAuths.keySet()){
            Address address = new Address(key);
            key_auths.put(address.getPublicKey(), keyAuths.get(key));
        }
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonElement toJsonObject() {
        return null;
    }
}
