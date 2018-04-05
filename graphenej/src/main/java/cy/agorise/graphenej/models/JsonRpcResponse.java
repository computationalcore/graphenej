package cy.agorise.graphenej.models;

/**
 * Used to represent a JSON-RPC response object
 */

public class JsonRpcResponse<T> {
    public long id;
    public Error error;
    public T result;

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
