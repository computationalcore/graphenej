package cy.agorise.graphenej.models;

/**
 * Generic witness response
 */
public class WitnessResponse<T> extends BaseResponse{
    public static final String KEY_ID = "id";
    public static final String KEY_RESULT = "result";

    public T result;
}
