package com.luminiasoft.bitshares.crypto;

import java.nio.ByteBuffer;

/**
 * A simple interface that can be used to retrieve entropy from any source.
 *
 * @author owlstead
 */
public interface EntropySource {
    /**
     * Retrieves the entropy.
     * The position of the ByteBuffer must be advanced to the limit by any users calling this method.
     * The values of the bytes between the position and limit should be set to zero by any users calling this method.
     *
     * @return entropy within the position and limit of the given buffer
     */
    ByteBuffer provideEntropy();
}