package de.bitsharesmunich.graphenej.models.backup;

import java.util.List;

/**
 * This class is used to represent the JSON-formatted version of the file backup containing one or more
 * wallets and keys.
 *
 * Created by nelson on 2/14/17.
 */
public class WalletBackup {
    private Wallet[] wallet;
    private PrivateKeyBackup[] private_keys;
    private LinkedAccount[] linked_accounts;

    public WalletBackup(List<Wallet> wallets, List<PrivateKeyBackup> privateKeys, List<LinkedAccount> linkedAccounts){
        this.wallet = wallets.toArray(new Wallet[wallets.size()]);
        this.private_keys = privateKeys.toArray(new PrivateKeyBackup[privateKeys.size()]);
        this.linked_accounts = linkedAccounts.toArray(new LinkedAccount[linkedAccounts.size()]);
    }

    public Wallet[] getWallets(){
        return wallet;
    }

    public PrivateKeyBackup[] getPrivateKeys(){
        return private_keys;
    }

    public LinkedAccount[] getLinkedAccounts(){
        return linked_accounts;
    }

    public Wallet getWallet(int index){
        return wallet[index];
    }

    public PrivateKeyBackup getPrivateKeyBackup(int index){
        return private_keys[index];
    }

    public int getWalletCount(){
        return wallet.length;
    }

    public int getKeyCount(){
        return private_keys.length;
    }
}
