package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.errors.MalformedTransactionException;
import org.bitcoinj.core.ECKey;


/**
 * Created by nelson on 11/14/16.
 */
public abstract class TransactionBuilder {
    protected ECKey privateKey;
    protected BlockData blockData;

    public abstract Transaction build() throws MalformedTransactionException;
}
