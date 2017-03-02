package de.bitsharesmunich.graphenej.models;

import de.bitsharesmunich.graphenej.operations.TransferOperation;


/**
 * This class offers support to deserialization of transfer operations received by the API
 * method get_relative_account_history.
 *
 * More operations types might be listed in the response of that method, but by using this class
 * those will be filtered out of the parsed result.
 */
public class HistoricalTransfer {
    private String id;
    private TransferOperation op;
    public Object[] result;
    private long block_num;
    private long trx_in_block;
    private long op_in_trx;
    private long virtual_op;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransferOperation getOperation() {
        return op;
    }

    public void setOperation(TransferOperation op) {
        this.op = op;
    }

    public long getBlockNum() {
        return block_num;
    }

    public void setBlockNum(long block_num) {
        this.block_num = block_num;
    }

    public long getTransactionsInBlock() {
        return trx_in_block;
    }

    public void setTransactionsInBlock(long trx_in_block) {
        this.trx_in_block = trx_in_block;
    }

    public long getOperationsInTrx() {
        return op_in_trx;
    }

    public void setOperationsInTrx(long op_in_trx) {
        this.op_in_trx = op_in_trx;
    }

    public long getVirtualOp() {
        return virtual_op;
    }

    public void setVirtualOp(long virtual_op) {
        this.virtual_op = virtual_op;
    }
}