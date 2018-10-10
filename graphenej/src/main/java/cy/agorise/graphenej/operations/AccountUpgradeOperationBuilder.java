package cy.agorise.graphenej.operations;

import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.errors.MalformedOperationException;

/**
 * Created by henry on 19/5/2018.
 */

public class AccountUpgradeOperationBuilder extends BaseOperationBuilder {

    private UserAccount accountToUpgrade;
    private AssetAmount fee;
    private boolean isUpgrade = true;

    public AccountUpgradeOperationBuilder setAccountToUpgrade(UserAccount accountToUpgrade) {
        this.accountToUpgrade = accountToUpgrade;
        return this;
    }

    public AccountUpgradeOperationBuilder setFee(AssetAmount fee) {
        this.fee = fee;
        return this;
    }

    public AccountUpgradeOperationBuilder setIsUpgrade(Boolean isUpgrade) {
        this.isUpgrade = isUpgrade;
        return this;
    }

    @Override
    public AccountUpgradeOperation build(){
        AccountUpgradeOperation accountUpgrade;
        if(accountToUpgrade == null ){
            throw new MalformedOperationException("Missing account to upgrade information");
        }

        if(fee != null){
            accountUpgrade = new AccountUpgradeOperation(accountToUpgrade, isUpgrade, fee);
        }else{
            accountUpgrade = new AccountUpgradeOperation(accountToUpgrade, isUpgrade);
        }
        return accountUpgrade;
    }
}
