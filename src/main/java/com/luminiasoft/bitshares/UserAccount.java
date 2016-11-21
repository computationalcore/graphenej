package com.luminiasoft.bitshares;

import com.google.gson.JsonObject;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class tha represents a graphene user account.
 * Created by nelson on 11/8/16.
 */
public class UserAccount extends GrapheneObject implements ByteSerializable, JsonSerializable {

    /**
     * Constructor that expects a user account in the string representation.
     * That is in the 1.2.x format.
     * @param id: The string representing the account apiId.
     */
    public UserAccount(String id) {
        super(id);
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
}
