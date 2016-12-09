package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

/**
 * Created by nelson on 11/5/16.
 */
public abstract class BaseOperation implements ByteSerializable, JsonSerializable{

    protected OperationType type;

    public BaseOperation(OperationType type){
        this.type = type;
    }

    public byte getId() {
        return (byte) this.type.ordinal();
    }

    public abstract void setFee(AssetAmount assetAmount);

    public abstract byte[] toBytes();
}
