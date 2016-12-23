package de.bitsharesmunich.graphenej.models;

import com.google.gson.*;
import de.bitsharesmunich.graphenej.Asset;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nelson on 12/22/16.
 */
public class BucketObject {
    public static final String KEY_HIGH_BASE = "high_base";
    public static final String KEY_HIGH_QUOTE = "high_quote";
    public static final String KEY_LOW_BASE = "low_base";
    public static final String KEY_LOW_QUOTE = "low_quote";
    public static final String KEY_OPEN_BASE = "open_base";
    public static final String KEY_OPEN_QUOTE = "open_quote";
    public static final String KEY_CLOSE_BASE = "close_base";
    public static final String KEY_CLOSE_QUOTE = "close_quote";
    public static final String KEY_BASE_VOLUME = "base_volume";
    public static final String KEY_QUOTE_VOLUME = "quote_volume";
    public static final String KEY_BASE = "base";
    public static final String KEY_QUOTE = "quote";
    public static final String KEY_SECONDS = "seconds";
    public static final String KEY_OPEN = "open";
    public static final String KEY_KEY = "key";

    public String id;
    public Key key;
    public BigInteger high_base;
    public BigInteger high_quote;
    public BigInteger low_base;
    public BigInteger low_quote;
    public BigInteger open_base;
    public BigInteger open_quote;
    public BigInteger close_base;
    public BigInteger close_quote;
    public BigInteger base_volume;
    public BigInteger quote_volume;

    public static class Key {
        public Asset base;
        public Asset quote;
        public long seconds;
        public Date open;
    }

    public static class BucketDeserializer implements JsonDeserializer<BucketObject> {

        @Override
        public BucketObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonBucket = json.getAsJsonObject();
            BucketObject bucket = new BucketObject();
            bucket.high_base = jsonBucket.get(KEY_HIGH_BASE).getAsBigInteger();
            bucket.high_quote = jsonBucket.get(KEY_HIGH_QUOTE).getAsBigInteger();
            bucket.low_base = jsonBucket.get(KEY_LOW_BASE).getAsBigInteger();
            bucket.low_quote = jsonBucket.get(KEY_LOW_QUOTE).getAsBigInteger();
            bucket.open_base = jsonBucket.get(KEY_OPEN_BASE).getAsBigInteger();
            bucket.open_quote = jsonBucket.get(KEY_OPEN_QUOTE).getAsBigInteger();
            bucket.close_base = jsonBucket.get(KEY_CLOSE_BASE).getAsBigInteger();
            bucket.close_quote = jsonBucket.get(KEY_CLOSE_QUOTE).getAsBigInteger();
            bucket.base_volume = jsonBucket.get(KEY_BASE_VOLUME).getAsBigInteger();
            bucket.quote_volume = jsonBucket.get(KEY_QUOTE_VOLUME).getAsBigInteger();
            bucket.key = new Key();
            String baseId = jsonBucket.get(KEY_KEY).getAsJsonObject().get(KEY_BASE).getAsString();
            String quoteId = jsonBucket.get(KEY_KEY).getAsJsonObject().get(KEY_QUOTE).getAsString();
            bucket.key.base = new Asset(baseId);
            bucket.key.quote = new Asset(quoteId);
            bucket.key.seconds = jsonBucket.get(KEY_KEY).getAsJsonObject().get(KEY_SECONDS).getAsLong();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                bucket.key.open = dateFormat.parse(jsonBucket.get(KEY_KEY).getAsJsonObject().get(KEY_OPEN).getAsString());
            } catch (ParseException e) {
                System.out.println("ParseException while deserializing BucketObject. Msg: "+e.getMessage());
            }
            return bucket;
        }
    }
}
