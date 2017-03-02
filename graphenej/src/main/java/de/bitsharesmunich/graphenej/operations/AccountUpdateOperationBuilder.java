package de.bitsharesmunich.graphenej.operations;

import de.bitsharesmunich.graphenej.*;
import de.bitsharesmunich.graphenej.errors.MalformedOperationException;

/**
 * Created by nelson on 3/1/17.
 */
public class AccountUpdateOperationBuilder extends BaseOperationBuilder {
    private AssetAmount fee;
    private UserAccount account;
    private Authority owner;
    private Authority active;
    private AccountOptions new_options;

    public AccountUpdateOperationBuilder setFee(AssetAmount fee) {
        this.fee = fee;
        return this;
    }

    public AccountUpdateOperationBuilder setAccount(UserAccount account) {
        this.account = account;
        return this;
    }

    public AccountUpdateOperationBuilder setOwner(Authority owner) {
        this.owner = owner;
        return this;
    }

    public AccountUpdateOperationBuilder setActive(Authority active) {
        this.active = active;
        return this;
    }

    public AccountUpdateOperationBuilder setOptions(AccountOptions newOptions) {
        this.new_options = newOptions;
        return this;
    }

    @Override
    public AccountUpdateOperation build() {
        AccountUpdateOperation operation;
        if(this.account == null){
            throw new MalformedOperationException("This operation requires an account to be set");
        }else{
            if(owner != null || active != null || new_options != null){
                if(fee == null){
                    operation = new AccountUpdateOperation(account, owner, active, new_options);
                }else{
                    operation = new AccountUpdateOperation(account, owner, active, new_options, fee);
                }
            }else{
                throw new MalformedOperationException("This operation requires at least either an authority or account options change");
            }
        }
        return operation;
    }
}
