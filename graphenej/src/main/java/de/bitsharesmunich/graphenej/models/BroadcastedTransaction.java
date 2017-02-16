package de.bitsharesmunich.graphenej.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.bitsharesmunich.graphenej.GrapheneObject;
import de.bitsharesmunich.graphenej.Transaction;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by nelson on 1/28/17.
 */
public class BroadcastedTransaction extends GrapheneObject implements Serializable {
    public static final String KEY_TRX = "trx";
    public static final String KEY_TRX_ID = "trx_id";

    private Transaction trx;
    private String trx_id;

    public BroadcastedTransaction(String id){
        super(id);
    }

    public void setTransaction(Transaction t){
        this.trx = t;
    }

    public Transaction getTransaction() {
        return trx;
    }

    public void setTransactionId(String id){
        this.trx_id = id;
    }

    public String getTransactionId() {
        return trx_id;
    }
}
