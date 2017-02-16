package de.bitsharesmunich.graphenej.crypto;

import java.security.SecureRandom;

/**
 * Created by nelson on 12/20/16.
 */
public class SecureRandomGenerator {

    public static SecureRandom getSecureRandom(){
        SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
//        randomStrengthener.addEntropySource(new AndroidRandomSource());
        return randomStrengthener.generateAndSeedRandomNumberGenerator();
    }
}
