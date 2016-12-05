package com.luminiasoft.bitshares.models;

import com.luminiasoft.bitshares.TransferOperation;

/**
 * This class offers support to deserialization of transfer operations received by the API
 * method get_relative_account_history.
 *
 * More operations types might be listed in the response of that method, but by using this class
 * those will be filtered out of the parsed result.
 */
public class HistoricalTransfer {
    public String id;
    public TransferOperation op;
    public Object[] result;
    public long block_num;
    public long trx_in_block;
    public long op_in_trx;
    public long virtual_op;
}
