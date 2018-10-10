package cy.agorise.graphenej.api;

/**
 * Class used to send connection status updates.
 *
 * Connection status updates can be any of the following:
 * - {@link ConnectionStatusUpdate#CONNECTED}
 * - {@link ConnectionStatusUpdate#AUTHENTICATED}
 * - {@link ConnectionStatusUpdate#API_UPDATE}
 * - {@link ConnectionStatusUpdate#DISCONNECTED}
 *
 * This is specified by the field called {@link #updateCode}.
 *
 * If the updateCode is ConnectionStatusUpdate#API_UPDATE another extra field called
 * {@link #api} is used to specify which api we're getting access to.
 */

public class ConnectionStatusUpdate {
    // Constant used to announce that a connection has been established
    public final static int CONNECTED = 0;
    // Constant used to announce a successful authentication
    public final static int AUTHENTICATED = 1;
    // Constant used to announce an api update
    public final static int API_UPDATE = 2;
    // Constant used to announce a disconnection event
    public final static int DISCONNECTED = 3;

    /**
     * The update code is the general purpose of the update message. Can be any of the following:
     * - {@link ConnectionStatusUpdate#CONNECTED}
     * - {@link ConnectionStatusUpdate#AUTHENTICATED}
     * - {@link ConnectionStatusUpdate#API_UPDATE}
     * - {@link ConnectionStatusUpdate#DISCONNECTED}
     */
    private int updateCode;

    /**
     * This field is used in case the updateCode is {@link ConnectionStatusUpdate#API_UPDATE} and
     * it serves to specify which API we're getting access to.
     *
     * It can be any of the fields defined in {@link ApiAccess}
     */
    private int api;

    public ConnectionStatusUpdate(int updateCode, int api){
        this.updateCode = updateCode;
        this.api = api;
    }

    public int getUpdateCode() {
        return updateCode;
    }

    public void setUpdateCode(int updateCode) {
        this.updateCode = updateCode;
    }

    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }
}
