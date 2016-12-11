package de.bitsharesmunich.graphenej;

import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;

/**
 * This class encapsulates all block-related information needed in order to build a valid transaction.
 */
public class BlockData implements ByteSerializable {
    private final int REF_BLOCK_NUM_BYTES = 2;
    private final int REF_BLOCK_PREFIX_BYTES = 4;
    private final int REF_BLOCK_EXPIRATION_BYTES = 4;

    private int refBlockNum;
    private long refBlockPrefix;
    private long relativeExpiration;

    /**
     * Block data constructor
     * @param ref_block_num: Least significant 16 bits from the reference block number.
     *                     If "relative_expiration" is zero, this field must be zero as well.
     * @param ref_block_prefix: The first non-block-number 32-bits of the reference block ID.
     *                        Recall that block IDs have 32 bits of block number followed by the
     *                        actual block hash, so this field should be set using the second 32 bits
     *                        in the block_id_type
     * @param relative_expiration: This field specifies the number of block intervals after the
     *                           reference block until this transaction becomes invalid. If this field is
     *                           set to zero, the "ref_block_prefix" is interpreted as an absolute timestamp
     *                           of the time the transaction becomes invalid.
     */
    public BlockData(int ref_block_num, long ref_block_prefix, long relative_expiration){
        this.refBlockNum = ref_block_num;
        this.refBlockPrefix = ref_block_prefix;
        this.relativeExpiration = relative_expiration;
    }

    /**
     * Block data constructor that takes in raw blockchain information.
     * @param head_block_number: The last block number.
     * @param head_block_id: The last block apiId.
     * @param relative_expiration: The relative expiration
     */
    public BlockData(long head_block_number, String head_block_id, long relative_expiration){
        String hashData = head_block_id.substring(8, 16);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 8; i = i + 2){
            builder.append(hashData.substring(6 - i, 8 - i));
        }
        this.refBlockNum = ((int) head_block_number ) & 0xFFFF;
        this.refBlockPrefix = Long.parseLong(builder.toString(), 16);
        this.relativeExpiration = relative_expiration;
    }

    public int getRefBlockNum() {
        return refBlockNum;
    }

    public void setRefBlockNum(int refBlockNum) {
        this.refBlockNum = refBlockNum;
    }

    public long getRefBlockPrefix() {
        return refBlockPrefix;
    }

    public void setRefBlockPrefix(long refBlockPrefix) {
        this.refBlockPrefix = refBlockPrefix;
    }

    public long getRelativeExpiration() {
        return relativeExpiration;
    }

    public void setRelativeExpiration(long relativeExpiration) {
        this.relativeExpiration = relativeExpiration;
    }


    @Override
    public byte[] toBytes() {
        // Allocating a fixed length byte array, since we will always need
        // 2 bytes for the ref_block_num value
        // 4 bytes for the ref_block_prefix value
        // 4 bytes for the relative_expiration
        
        byte[] result = new byte[REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES + REF_BLOCK_EXPIRATION_BYTES];
        for(int i = 0; i < result.length; i++){
            if(i < REF_BLOCK_NUM_BYTES){
                result[i] = (byte) (this.refBlockNum >> 8 * i);
            }else if(i >= REF_BLOCK_NUM_BYTES && i < REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES){
                result[i] = (byte) (this.refBlockPrefix >> 8 * (i - REF_BLOCK_NUM_BYTES));
            }else{
                result[i] = (byte) (this.relativeExpiration >> 8 * (i - REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES));
            }
        }
        return result;
    }
}
