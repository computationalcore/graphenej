package de.bitsharesmunich.graphenej.models.backup;

import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Util;
import de.bitsharesmunich.graphenej.crypto.SecureRandomGenerator;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.spongycastle.crypto.digests.SHA256Digest;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class holds data deserialized from a wallet in the backup file.
 *
 * Created by nelson on 2/14/17.
 */
public class Wallet {
    private String public_name;
    private String password_pubkey;
    private String encryption_key;
    private String encrypted_brainkey;
    private String brainkey_pubkey;
    private int brainkey_sequence;
    private String brainkey_backup_date;
    private String created;
    private String last_modified;
    private String chain_id;
    private String id;
    private String backup_date;

    /**
     * No args constructor
     */
    public Wallet(){}

    public Wallet(String name){
        this.public_name = name;
        this.id = name;
    }

    /**
     * Wallet constructor that takes a few arguments.
     * @param name: The name of this wallet.
     * @param brainKey: The brain key to be used.
     * @param brainkeySequence: The brain key sequence.
     * @param chainId: The chain id
     * @param password: Password used to encrypt all sensitive data.
     */
    public Wallet(String name, String brainKey, int brainkeySequence, String chainId, String password){
        this(name);
        SecureRandom secureRandom = SecureRandomGenerator.getSecureRandom();
        byte[] decryptedKey = new byte[Util.KEY_LENGTH];
        secureRandom.nextBytes(decryptedKey);
        this.encryption_key = Util.bytesToHex(Util.encryptAES(decryptedKey, password.getBytes()));
        this.encrypted_brainkey = Util.bytesToHex(Util.encryptAES(brainKey.getBytes(), decryptedKey));
        this.brainkey_sequence = brainkeySequence;
        this.chain_id = chainId;

        try {
            byte[] passwordHash = Sha256Hash.hash(password.getBytes("UTF8"));
            this.password_pubkey = new Address(ECKey.fromPublicOnly(ECKey.fromPrivate(passwordHash).getPubKey())).toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try{
            byte[] brainkeyHash = Sha256Hash.hash(brainKey.getBytes("UTF8"));
            this.brainkey_pubkey = new Address(ECKey.fromPublicOnly(ECKey.fromPrivate(brainkeyHash).getPubKey())).toString();
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
        this.created = dateFormat.format(now);
        this.last_modified = created;
        this.backup_date = created;
        this.brainkey_backup_date = created;
    }

    /**
     * Method that will return the decrypted version of the "encrypted_brainkey" field.
     * @param password: Password used to encrypt the encryption key.
     * @return: The brainkey in its plaintext version.
     */
    public String decryptBrainKey(String password){
        byte[] decryptedKey = getEncryptionKey(password);
        byte[] encryptedBrainKey = Util.hexToBytes(encrypted_brainkey);
        return new String(Util.decryptAES(encryptedBrainKey, decryptedKey));
    }

    /**
     * Decrypts the encryption key, which is also provided in an encrypted form.
     * @param password: The password used to encrypt the encryption key.
     * @return: The encryption key.
     */
    public byte[] getEncryptionKey(String password){
        return Util.decryptAES(Util.hexToBytes(encryption_key), password.getBytes());
    }

    public String getPrivateName() {
        return public_name;
    }

    public void setPrivateName(String privateName) {
        this.public_name = privateName;
    }

    public String getPasswordPubkey() {
        return password_pubkey;
    }

    public void setPasswordPubkey(String password_pubkey) {
        this.password_pubkey = password_pubkey;
    }

    /**
     * Gets the cyphertext version of the encryption key.
     * @return: Encryption key in its cyphertext version.
     */
    public String getEncryptionKey() {
        return encryption_key;
    }

    public void setEncryptionKey(String encryption_key) {
        this.encryption_key = encryption_key;
    }

    public String getEncryptedBrainkey() {
        return encrypted_brainkey;
    }

    public void setEncryptedBrainkey(String encrypted_brainkey) {
        this.encrypted_brainkey = encrypted_brainkey;
    }

    public String getBrainkeyPubkey() {
        return brainkey_pubkey;
    }

    public void setBrainkeyPubkey(String brainkey_pubkey) {
        this.brainkey_pubkey = brainkey_pubkey;
    }

    public int getBrainkeySequence() {
        return brainkey_sequence;
    }

    public void setBrainkeySequence(int brainkey_sequence) {
        this.brainkey_sequence = brainkey_sequence;
    }

    public String getBrainkeyBackup_date() {
        return brainkey_backup_date;
    }

    public void setBrainkeyBackupDate(String brainkey_backup_date) {
        this.brainkey_backup_date = brainkey_backup_date;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastModified() {
        return last_modified;
    }

    public void setLastModified(String last_modified) {
        this.last_modified = last_modified;
    }

    public String getChainId() {
        return chain_id;
    }

    public void setChainId(String chain_id) {
        this.chain_id = chain_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBackupDate() {
        return backup_date;
    }

    public void setBackupDate(String backup_date) {
        this.backup_date = backup_date;
    }
}
