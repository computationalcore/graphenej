package de.bitsharesmunich.graphenej.objects;

import de.bitsharesmunich.graphenej.PublicKey;

/**
 * Class to build a Memo Object
 * @author henry 10/12/2016
 */
public class MemoBuilder {

    private PublicKey fromKey;
    private PublicKey toKey;
    private String message;
    private long nonce = 0;

    /**
     * Empty Constructor
     */
    public MemoBuilder() {
    }

    /**
     * Set the key of the Source, needs to have a private Key access
     * @param fromKey The Public Key of the sender
     * @return The MemoBuilder
     */
    public MemoBuilder setFromKey(PublicKey fromKey) {
        this.fromKey = fromKey;
        return this;
    }

    /**
     * Set the key of the destination, only need the public key.
     * @param toKey The Public Key of the receiver
     * @return  The MemoBuilder
     */
    public MemoBuilder setToKey(PublicKey toKey) {
        this.toKey = toKey;
        return this;
    }

    /**
     * Set the message to be send
     * @param message The message as a String
     * @return  The MemoBuilder
     */
    public MemoBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * (Optional) Sets a custom nonce
     * @param nonce The custom nonce
     * @return The MemoBuilder
     */
    public MemoBuilder setNone(Long nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * Biulds the memo object
     * @return The Memo object
     */
    public Memo build() {
        //Todo unencode key
        if (nonce == 0) {
            return Memo.encodeMessage(fromKey, toKey, message.getBytes());
        }
        return Memo.encodeMessage(fromKey, toKey, message.getBytes(), nonce);
    }

}
