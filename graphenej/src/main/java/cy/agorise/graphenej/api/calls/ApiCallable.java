package cy.agorise.graphenej.api.calls;

import cy.agorise.graphenej.models.ApiCall;

/**
 * Interface to be implemented by all classes that will produce an ApiCall object instance
 * as a result.
 */

public interface ApiCallable {

    /**
     *
     * @return An instance of the {@link ApiCall} class
     */
    ApiCall toApiCall(int apiId, long sequenceId);
}
