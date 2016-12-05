package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.errors.MalformedTransactionException;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to build a transaction containing a transfer operation.
 */
public class TransferTransactionBuilder extends TransactionBuilder {
    private List<BaseOperation> operations;
    private UserAccount sourceAccount;
    private UserAccount destinationAccount;
    private AssetAmount transferAmount;
    private AssetAmount feeAmount;

    public TransferTransactionBuilder(){}

    public TransferTransactionBuilder(ECKey privKey) {
        super(privKey);
    }

    public TransferTransactionBuilder setPrivateKey(ECKey key){
        this.privateKey = key;
        return this;
    }

    public TransferTransactionBuilder setBlockData(BlockData blockData){
        this.blockData = blockData;
        return this;
    }

    public TransferTransactionBuilder setSource(UserAccount source){
        this.sourceAccount = source;
        return this;
    }

    public TransferTransactionBuilder setDestination(UserAccount destination){
        this.destinationAccount = destination;
        return this;
    }

    public TransferTransactionBuilder setAmount(AssetAmount amount){
        this.transferAmount = amount;
        return this;
    }

    public TransferTransactionBuilder setFee(AssetAmount amount){
        this.feeAmount = amount;
        return this;
    }

    //TODO: Add support for multiple transfer operations in a single transaction
    public TransferTransactionBuilder addOperation(TransferOperation transferOperation){
        if(operations == null){
            operations = new ArrayList<BaseOperation>();
        }
        return this;
    }

    @Override
    public Transaction build() throws MalformedTransactionException {
        if(privateKey == null){
            throw new MalformedTransactionException("Missing private key information");
        }else if(operations == null){
            // If the operations list has not been set, we might be able to build one with the
            // previously provided data. But in order for this to work we have to have all
            // source, destination and transfer amount data.
            operations = new ArrayList<>();
            if(sourceAccount == null){
                throw new MalformedTransactionException("Missing source account information");
            }
            if(destinationAccount == null){
                throw new MalformedTransactionException("Missing destination account information");
            }
            if(transferAmount == null){
                throw new MalformedTransactionException("Missing transfer amount information");
            }
            TransferOperation transferOperation;
            if(feeAmount == null){
                transferOperation = new TransferOperation(sourceAccount, destinationAccount, transferAmount);
            }else{
                transferOperation = new TransferOperation(sourceAccount, destinationAccount, transferAmount, feeAmount);
            }
            operations.add(transferOperation);
        }
        return new Transaction(privateKey, blockData, operations);
    }
}
