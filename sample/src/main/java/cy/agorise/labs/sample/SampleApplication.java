package cy.agorise.labs.sample;

import android.app.Application;

import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.api.android.NetworkServiceManager;

/**
 * Sample application class
 */

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Specifying some important information regarding the connection, such as the
        // credentials and the requested API accesses
        int requestedApis = ApiAccess.API_DATABASE | ApiAccess.API_HISTORY | ApiAccess.API_NETWORK_BROADCAST;

        NetworkServiceManager networkManager = new NetworkServiceManager.Builder()
                .setUserName("username")
                .setPassword("secret")
                .setRequestedApis(requestedApis)
                .setCustomNodeUrls("wss://eu.nodes.bitshares.ws")
                .setAutoConnect(true)
                .setNodeLatencyVerification(true)
                .build(this);

        // Registering this class as a listener to all activity's callback cycle events, in order to
        // better estimate when the user has left the app and it is safe to disconnect the websocket connection
        registerActivityLifecycleCallbacks(networkManager);
    }
}
