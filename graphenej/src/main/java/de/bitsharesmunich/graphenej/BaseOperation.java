package de.bitsharesmunich.graphenej;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;

/**
 * Created by nelson on 11/5/16.
 */
public abstract class BaseOperation implements ByteSerializable, JsonSerializable {

    public static final String KEY_FEE = "fee";
    public static final String KEY_EXTENSIONS = "extensions";

    protected OperationType type;
    protected Extensions extensions;

    public BaseOperation(OperationType type){
        this.type = type;
        this.extensions = new Extensions();
    }

    public byte getId() {
        return (byte) this.type.ordinal();
    }

    public abstract void setFee(AssetAmount assetAmount);

    public JsonElement toJsonObject(){
        JsonArray array = new JsonArray();
        array.add(this.getId());
        return array;
    }
}
