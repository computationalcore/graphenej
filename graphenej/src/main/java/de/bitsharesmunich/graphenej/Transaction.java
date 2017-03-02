package de.bitsharesmunich.graphenej;

import com.google.common.primitives.Bytes;
import com.google.gson.*;
import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;

import de.bitsharesmunich.graphenej.operations.TransferOperation;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.lang.reflect.Type;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class used to represent a generic Graphene transaction.
 */
public class Transaction implements ByteSerializable, JsonSerializable {

    /* Default expiration time */
    public static final int DEFAULT_EXPIRATION_TIME = 30;

    /* Constant field names used for serialization/deserialization purposes */
    public static final String KEY_EXPIRATION = "expiration";
    public static final String KEY_SIGNATURES = "signatures";
    public static final String KEY_OPERATIONS = "operations";
    public static final String KEY_EXTENSIONS = "extensions";
    public static final String KEY_REF_BLOCK_NUM = "ref_block_num";
    public static final String KEY_REF_BLOCK_PREFIX = "ref_block_prefix";

    private ECKey privateKey;
    private BlockData blockData;
    private List<BaseOperation> operations;
    private Extensions extensions;

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
        this.extensions = new Extensions();
    }

    /**
     * Transaction constructor.
     * @param wif: The user's private key in the base58 format.
     * @param block_data: Block data containing important information used to sign a transaction.
     * @param operation_list: List of operations to include in the transaction.
     */
    public Transaction(String wif, BlockData block_data, List<BaseOperation> operation_list){
        this(DumpedPrivateKey.fromBase58(null, wif).getKey(), block_data, operation_list);
    }

    /**
     * Constructor used to build a Transaction object without a private key. This kind of object
     * is used to represent a transaction data that we don't intend to serialize and sign.
     * @param blockData: Block data instance, containing information about the location of this transaction in the blockchain.
     * @param operationList: The list of operations included in this transaction.
     */
    public Transaction(BlockData blockData, List<BaseOperation> operationList){
        this.blockData = blockData;
        this.operations = operationList;
    }

    /**
     * Updates the block data
     * @param blockData: New block data
     */
    public void setBlockData(BlockData blockData){
        this.blockData = blockData;
    }

    /**
     * Updates the fees for all operations in this transaction.
     * @param fees: New fees to apply
     */
    public void setFees(List<AssetAmount> fees){
        for(int i = 0; i < operations.size(); i++)
            operations.get(i).setFee(fees.get(i));
    }

    public ECKey getPrivateKey(){
        return this.privateKey;
    }

    public List<BaseOperation> getOperations(){ return this.operations; }

    /**
     * This method is used to query whether the instance has a private key.
     * @return
     */
    public boolean hasPrivateKey(){
        return this.privateKey != null;
    }

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
                this.blockData.setExpiration(this.blockData.getExpiration() + 1);
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

        // Adding extensions byte
        byteArray.addAll(Bytes.asList(this.extensions.toBytes()));

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
        Date expirationTime = new Date(blockData.getExpiration() * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
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

    /**
     * Class used to encapsulate the procedure to be followed when converting a transaction from a
     * java object to its JSON string format representation.
     */
    public static class TransactionSerializer implements JsonSerializer<Transaction> {

        @Override
        public JsonElement serialize(Transaction transaction, Type type, JsonSerializationContext jsonSerializationContext) {
            return transaction.toJsonObject();
        }
    }

    /**
     * Static inner class used to encapsulate the procedure to be followed when converting a transaction from its
     * JSON string format representation into a java object instance.
     */
    public static class TransactionDeserializer implements JsonDeserializer<Transaction> {

        @Override
        public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Parsing block data information
            int refBlockNum = jsonObject.get(KEY_REF_BLOCK_NUM).getAsInt();
            long refBlockPrefix = jsonObject.get(KEY_REF_BLOCK_PREFIX).getAsLong();
            String expiration = jsonObject.get(KEY_EXPIRATION).getAsString();
            SimpleDateFormat dateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date expirationDate = dateFormat.parse(expiration, new ParsePosition(0));
            BlockData blockData = new BlockData(refBlockNum, refBlockPrefix, expirationDate.getTime());

            // Parsing operation list
            BaseOperation operation = null;
            ArrayList<BaseOperation> operationList = new ArrayList<>();
            try {
                for (JsonElement jsonOperation : jsonObject.get(KEY_OPERATIONS).getAsJsonArray()) {
                    int operationId = jsonOperation.getAsJsonArray().get(0).getAsInt();
                    if (operationId == OperationType.TRANSFER_OPERATION.ordinal()) {
                        System.out.println("Transfer operation detected!");
                        operation = context.deserialize(jsonOperation, TransferOperation.class);
                    } else if (operationId == OperationType.LIMIT_ORDER_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.LIMIT_ORDER_CANCEL_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.CALL_ORDER_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.FILL_ORDER_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ACCOUNT_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ACCOUNT_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ACCOUNT_WHITELIST_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ACCOUNT_UPGRADE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ACCOUNT_TRANSFER_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_UPDATE_BITASSET_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_UPDATE_FEED_PRODUCERS_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_ISSUE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_RESERVE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_FUND_FEE_POOL_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_SETTLE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_GLOBAL_SETTLE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_PUBLISH_FEED_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WITNESS_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WITNESS_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.PROPOSAL_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.PROPOSAL_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.PROPOSAL_DELETE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WITHDRAW_PERMISSION_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WITHDRAW_PERMISSION_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WITHDRAW_PERMISSION_CLAIM_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WITHDRAW_PERMISSION_DELETE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.COMMITTEE_MEMBER_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.COMMITTEE_MEMBER_UPDATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.COMMITTEE_MEMBER_UPDATE_GLOBAL_PARAMETERS_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.VESTING_BALANCE_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.VESTING_BALANCE_WITHDRAW_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.WORKER_CREATE_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.CUSTOM_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSERT_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.BALANCE_CLAIM_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.OVERRIDE_TRANSFER_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.TRANSFER_TO_BLIND_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.BLIND_TRANSFER_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.TRANSFER_FROM_BLIND_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_SETTLE_CANCEL_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    } else if (operationId == OperationType.ASSET_CLAIM_FEES_OPERATION.ordinal()) {
                        //TODO: Add operation deserialization support
                    }
                    if (operation != null) operationList.add(operation);
                    operation = null;
                }
                return new Transaction(blockData, operationList);
            }catch(Exception e){
                System.out.println("Exception. Msg: "+e.getMessage());
                for(StackTraceElement el : e.getStackTrace()){
                    System.out.println(el.getFileName()+"#"+el.getMethodName()+":"+el.getLineNumber());
                }
            }
            return new Transaction(blockData, operationList);
        }
    }
}