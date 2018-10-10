package cy.agorise.graphenej.models;

/**
 * Generic witness response
 * @deprecated Use {@link JsonRpcResponse} instead
 */
public class WitnessResponse<T> extends BaseResponse{
    public static final String KEY_ID = "id";
    public static final String KEY_RESULT = "result";

    public T result;
}
