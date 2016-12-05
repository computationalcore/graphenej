package com.luminiasoft.bitshares;

import com.google.gson.JsonElement;
import com.luminiasoft.bitshares.interfaces.GrapheneSerializable;

/**
 * Used whenever we have an optional field.
 */
public class Optional<T extends GrapheneSerializable> implements GrapheneSerializable {
    private T optionalField;

    public Optional(T field){
        optionalField = field;
    }

    @Override
    public byte[] toBytes() {
        if(optionalField == null)
            return new byte[] { (byte) 0 };
        else
            return optionalField.toBytes();
    }

    @Override
    public String toJsonString() {
        return optionalField.toJsonString();
    }

    @Override
    public JsonElement toJsonObject() {
        return optionalField.toJsonObject();
    }
}
