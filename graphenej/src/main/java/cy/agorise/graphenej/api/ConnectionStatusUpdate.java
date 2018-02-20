package cy.agorise.graphenej.api;

/**
 * Class used to send connection status updates
 */

public class ConnectionStatusUpdate {
    public final static String CONNECTED = "Connected";
    public final static String DISCONNECTED = "Disconnected";

    private String connectionStatus;

    public ConnectionStatusUpdate(String status){
        this.connectionStatus = status;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
}
