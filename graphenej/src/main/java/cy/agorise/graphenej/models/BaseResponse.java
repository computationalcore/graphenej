package cy.agorise.graphenej.models;

/**
 * Base response class
 * @deprecated Use {@link JsonRpcResponse} instead
 */
public class BaseResponse {
    public long id;
    public Error error;

    public static class Error {
        public ErrorData data;
        public int code;
        public String message;
        public Error(String message){
            this.message = message;
        }
    }

    public static class ErrorData {
        public int code;
        public String name;
        public String message;
        //TODO: Include stack data

        public ErrorData(String message){
            this.message = message;
        }
    }
}
