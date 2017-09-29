package de.bitsharesmunich.graphenej.interfaces;

import de.bitsharesmunich.graphenej.models.BaseResponse;

/**
 * Interface to be implemented by any listener to network errors.
 */
public interface NodeErrorListener {
    void onError(BaseResponse.Error error);
}
