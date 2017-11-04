package cy.agorise.graphenej.errors;

/**
 * Created by nelson on 1/18/17.
 */
public class IncompatibleOperation extends RuntimeException {

    public IncompatibleOperation(String message){
        super(message);
    }
}
