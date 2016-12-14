package de.bitsharesmunich.graphenej.models;

/**
 * Created by nelson on 12/13/16.
 */
public class BlockHeader {
    public String previous;
    public String timestamp;
    public String witness;
    public String transaction_merkle_root;
    public Object[] extension;
}
