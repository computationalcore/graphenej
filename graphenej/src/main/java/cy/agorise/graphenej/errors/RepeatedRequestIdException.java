package cy.agorise.graphenej.errors;

import cy.agorise.graphenej.api.BaseGrapheneHandler;

/**
 * Thrown by the {@link cy.agorise.graphenej.api.SubscriptionMessagesHub#addRequestHandler(BaseGrapheneHandler)}
 * whenever the user tries to register a new handler with a previously registered id
 */

public class RepeatedRequestIdException extends Exception {
    public RepeatedRequestIdException(String message){
        super(message);
    }
}
