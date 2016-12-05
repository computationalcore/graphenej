package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.errors.MalformedTransactionException;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to build a transaction containing an account update operation.
 */
public class AccountUpdateTransactionBuilder extends TransactionBuilder {
    private List<BaseOperation> operations;
    private AssetAmount fee;
    private UserAccount account;
    private Authority owner;
    private Authority active;
    private AccountOptions new_options;

    public AccountUpdateTransactionBuilder(ECKey privKey) {
        super(privKey);
    }


    public AccountUpdateTransactionBuilder setAccont(UserAccount account){
        this.account = account;
        return this;
    }

    public AccountUpdateTransactionBuilder setOwner(Authority owner){
        this.owner = owner;
        return this;
    }

    public AccountUpdateTransactionBuilder setActive(Authority active){
        this.active = active;
        return this;
    }

    public AccountUpdateTransactionBuilder setOptions(AccountOptions options){
        this.new_options = options;
        return this;
    }

    public AccountUpdateTransactionBuilder setFee(AssetAmount fee){
        this.fee = fee;
        return this;
    }

    @Override
    public Transaction build() throws MalformedTransactionException {
        if(account == null){
            throw new MalformedTransactionException("Missing required account information");
        }else{
            operations = new ArrayList<>();
            AccountUpdateOperation operation;
            if(fee == null){
                operation = new AccountUpdateOperation(account, owner, active, new_options);
            }else{
                operation = new AccountUpdateOperation(account, owner, active, new_options, fee);
            }
            operations.add(operation);
        }
        return new Transaction(privateKey, blockData, operations);
    }
}
