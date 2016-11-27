package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class used to represent a generic graphene transaction.
 */
public class Transaction implements ByteSerializable, JsonSerializable {
    private final String TAG = this.getClass().getName();

    public static final String KEY_EXPIRATION = "expiration";
    public static final String KEY_SIGNATURES = "signatures";
    public static final String KEY_OPERATIONS = "operations";
    public static final String KEY_EXTENSIONS = "extensions";
    public static final String KEY_REF_BLOCK_NUM = "ref_block_num";
    public static final String KEY_REF_BLOCK_PREFIX = "ref_block_prefix";

    private ECKey privateKey;
    private BlockData blockData;
    private List<BaseOperation> operations;
    private List<Extension> extensions;

    /**
     * Transaction constructor.
     * @param wif: The user's private key in the base58 format.
     * @param block_data: Block data containing important information used to sign a transaction.
     * @param operation_list: List of operations to include in the transaction.
     */
    public Transaction(String wif, BlockData block_data, List<BaseOperation> operation_list){
        this.privateKey = DumpedPrivateKey.fromBase58(null, wif).getKey();
        this.blockData = block_data;
        this.operations = operation_list;
        this.extensions = new ArrayList<Extension>();
    }

    /**
     * Transaction constructor.
     * @param privateKey : Instance of a ECKey containing the private key that will be used to sign this transaction.
     * @param blockData : Block data containing important information used to sign a transaction.
     * @param operationList : List of operations to include in the transaction.
     */
    public Transaction(ECKey privateKey, BlockData blockData, List<BaseOperation> operationList){
        this.privateKey = privateKey;
        this.blockData = blockData;
        this.operations = operationList;
        this.extensions = new ArrayList<Extension>();
    }

    public ECKey getPrivateKey(){
        return this.privateKey;
    }

    public List<BaseOperation> getOperations(){ return this.operations; }

    /**
     * Obtains a signature of this transaction. Please note that due to the current reliance on
     * bitcoinj to generate the signatures, and due to the fact that it uses deterministic
     * ecdsa signatures, we are slightly modifying the expiration time of the transaction while
     * we look for a signature that will be accepted by the graphene network.
     *
     * This should then be called before any other serialization method.
     * @return: A valid signature of the current transaction.
     */
    public byte[] getGrapheneSignature(){
        boolean isGrapheneCanonical = false;
        byte[] sigData = null;

        while(!isGrapheneCanonical) {
            byte[] serializedTransaction = this.toBytes();
            Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction));
            int recId = -1;
            ECKey.ECDSASignature sig = privateKey.sign(hash);

            // Now we have to work backwards to figure out the recId needed to recover the signature.
            for (int i = 0; i < 4; i++) {
                ECKey k = ECKey.recoverFromSignature(i, sig, hash, privateKey.isCompressed());
                if (k != null && k.getPubKeyPoint().equals(privateKey.getPubKeyPoint())) {
                    recId = i;
                    break;
                }
            }

            sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
            int headerByte = recId + 27 + (privateKey.isCompressed() ? 4 : 0);
            sigData[0] = (byte) headerByte;
            System.arraycopy(Utils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
            System.arraycopy(Utils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);

            // Further "canonicality" tests
            if(((sigData[0] & 0x80) != 0) || (sigData[0] == 0) ||
                    ((sigData[1] & 0x80) != 0) || ((sigData[32] & 0x80) != 0) ||
                    (sigData[32] == 0) || ((sigData[33] & 0x80)  != 0)){
                this.blockData.setRelativeExpiration(this.blockData.getRelativeExpiration() + 1);
            }else{
                isGrapheneCanonical = true;
            }
        }
        return sigData;
    }
    /**
     * Method that creates a serialized byte array with compact information about this transaction
     * that is needed for the creation of a signature.
     * @return: byte array with serialized information about this transaction.
     */
    public byte[] toBytes(){
        // Creating a List of Bytes and adding the first bytes from the chain apiId
        List<Byte> byteArray = new ArrayList<Byte>();
        byteArray.addAll(Bytes.asList(Util.hexToBytes(Chains.BITSHARES.CHAIN_ID)));

        // Adding the block data
        byteArray.addAll(Bytes.asList(this.blockData.toBytes()));

        // Adding the number of operations
        byteArray.add((byte) this.operations.size());

        // Adding all the operations
        for(BaseOperation operation : operations){
            byteArray.add(operation.getId());
            byteArray.addAll(Bytes.asList(operation.toBytes()));
        }

        //Adding the number of extensions
        byteArray.add((byte) this.extensions.size());

        for(Extension extension : extensions){
            //TODO: Implement the extension serialization
        }
        // Adding a last zero byte to match the result obtained by the python-graphenelib code
        // I'm not exactly sure what's the meaning of this last zero byte, but for now I'll just
        // leave it here and work on signing the transaction.
        //TODO: Investigate the origin and meaning of this last byte.
        byteArray.add((byte) 0 );

        return Bytes.toArray(byteArray);
    }

    @Override
    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Transaction.class, new TransactionSerializer());
        return gsonBuilder.create().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();

        // Getting the signature before anything else,
        // since this might change the transaction expiration data slightly
        byte[] signature = getGrapheneSignature();

        // Formatting expiration time
        Date expirationTime = new Date(blockData.getRelativeExpiration() * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Adding expiration
        obj.addProperty(KEY_EXPIRATION, dateFormat.format(expirationTime));

        // Adding signatures
        JsonArray signatureArray = new JsonArray();
        signatureArray.add(Util.bytesToHex(signature));
        obj.add(KEY_SIGNATURES, signatureArray);

        JsonArray operationsArray = new JsonArray();
        for(BaseOperation operation : operations){
            operationsArray.add(operation.toJsonObject());
        }
        // Adding operations
        obj.add(KEY_OPERATIONS, operationsArray);

        // Adding extensions
        obj.add(KEY_EXTENSIONS, new JsonArray());

        // Adding block data
        obj.addProperty(KEY_REF_BLOCK_NUM, blockData.getRefBlockNum());
        obj.addProperty(KEY_REF_BLOCK_PREFIX, blockData.getRefBlockPrefix());

        return obj;

    }

    class TransactionSerializer implements JsonSerializer<Transaction> {

        @Override
        public JsonElement serialize(Transaction transaction, Type type, JsonSerializationContext jsonSerializationContext) {
            return transaction.toJsonObject();
        }
    }
}