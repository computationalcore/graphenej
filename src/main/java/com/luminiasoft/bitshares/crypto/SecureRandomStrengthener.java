package com.luminiasoft.bitshares.crypto;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

/**
 * A strengthener that can be used to generate and re-seed random number
 * generators that do not seed themselves appropriately.
 *
 * @author owlstead
 */
public class SecureRandomStrengthener {
    private static final String DEFAULT_PSEUDO_RANDOM_NUMBER_GENERATOR = "SHA1PRNG";

    private static final EntropySource mTimeEntropySource = new EntropySource() {

        final ByteBuffer timeBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE
                * 2);

        @Override
        public ByteBuffer provideEntropy() {
            this.timeBuffer.clear();
            this.timeBuffer.putLong(System.currentTimeMillis());
            this.timeBuffer.putLong(System.nanoTime());
            this.timeBuffer.flip();
            return this.timeBuffer;
        }
    };

    private final String algorithm;
    private final List<EntropySource> entropySources = new LinkedList<EntropySource>();
    private final MessageDigest digest;
    private final ByteBuffer seedBuffer;

    /**
     * Generates an instance of a {@link SecureRandomStrengthener} that
     * generates and re-seeds instances of {@code "SHA1PRNG"}.
     *
     * @return the strengthener, never null
     */
    public static SecureRandomStrengthener getInstance() {
        return new SecureRandomStrengthener(
                DEFAULT_PSEUDO_RANDOM_NUMBER_GENERATOR);
    }

    /**
     * Generates an instance of a {@link SecureRandomStrengthener} that
     * generates instances of the given argument. Note that the availability of
     * the given algorithm arguments in not tested until generation.
     *
     * @param algorithm
     *            the algorithm indicating the {@link SecureRandom} instance to
     *            use
     * @return the strengthener, never null
     */
    public static SecureRandomStrengthener getInstance(final String algorithm) {
        return new SecureRandomStrengthener(algorithm);
    }

    private SecureRandomStrengthener(final String algorithm) {
        if (algorithm == null || algorithm.length() == 0) {
            throw new IllegalArgumentException(
                    "Please provide a PRNG algorithm string such as SHA1PRNG");
        }

        this.algorithm = algorithm;
        try {
            this.digest = MessageDigest.getInstance("SHA1");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "MessageDigest to create seed not available", e);
        }
        this.seedBuffer = ByteBuffer.allocate(this.digest.getDigestLength());
    }

    /**
     * Add an entropy source, which will be called for each generation and
     * re-seeding of the given random number generator.
     *
     * @param source
     *            the source of entropy
     */
    public void addEntropySource(final EntropySource source) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "EntropySource should not be null");
        }
        this.entropySources.add(source);
    }

    /**
     * Generates and seeds a random number generator of the configured
     * algorithm. Calls the {@link EntropySource#provideEntropy()} method of all
     * added sources of entropy.
     *
     * @return the random number generator
     */
    public SecureRandom generateAndSeedRandomNumberGenerator() {
        final SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance(this.algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("PRNG is not available", e);
        }

        reseed(secureRandom);
        return secureRandom;
    }

    /**
     * Re-seeds the random number generator. Calls the
     * {@link EntropySource#provideEntropy()} method of all added sources of
     * entropy.
     *
     * @param secureRandom
     *            the random number generator to re-seed
     */
    public void reseed(final SecureRandom secureRandom) {
        this.seedBuffer.clear();
        secureRandom.nextBytes(this.seedBuffer.array());

        for (final EntropySource source : this.entropySources) {
            final ByteBuffer entropy = source.provideEntropy();
            if (entropy == null) {
                continue;
            }

            final ByteBuffer wipeBuffer = entropy.duplicate();
            this.digest.update(entropy);
            wipe(wipeBuffer);
        }

        this.digest.update(mTimeEntropySource.provideEntropy());
        this.digest.update(this.seedBuffer);
        this.seedBuffer.clear();
        // remove data from seedBuffer so it won't be retrievable

        // reuse

        try {
            this.digest.digest(this.seedBuffer.array(), 0,
                    this.seedBuffer.capacity());
        } catch (final DigestException e) {
            throw new IllegalStateException(
                    "DigestException should not be thrown", e);
        }
        secureRandom.setSeed(this.seedBuffer.array());

        wipe(this.seedBuffer);
    }

    private void wipe(final ByteBuffer buf) {
        while (buf.hasRemaining()) {
            buf.put((byte) 0);
        }
    }
}