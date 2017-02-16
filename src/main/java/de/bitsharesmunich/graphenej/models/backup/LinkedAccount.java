package de.bitsharesmunich.graphenej.models.backup;

/**
 * Class used to represent an entry in the "linked_accounts" field of the JSON-formatted backup file.
 * Created by nelson on 2/15/17.
 */
public class LinkedAccount {
    private String name;
    private String chainId;

    public LinkedAccount(String name, String chainId){
        this.name = name;
        this.chainId = chainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }
}
