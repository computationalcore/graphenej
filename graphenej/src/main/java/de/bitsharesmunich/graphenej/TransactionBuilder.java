package de.bitsharesmunich.graphenej;

import de.bitsharesmunich.graphenej.errors.MalformedTransactionException;
import org.bitcoinj.core.ECKey;


/**
 * Created by nelson on 11/14/16.
 */
public abstract class TransactionBuilder {
    protected ECKey privateKey;
    protected BlockData blockData;

    public TransactionBuilder(){}

    public TransactionBuilder(ECKey privKey){
        this.privateKey = privKey;
    }

    public TransactionBuilder setBlockData(BlockData blockData){
        this.blockData = blockData;
        return this;
    }

    public abstract Transaction build() throws MalformedTransactionException;
}
