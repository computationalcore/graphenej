package com.luminiasoft.labs.sample;

import android.app.Application;
import android.preference.PreferenceManager;

import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.api.android.NetworkService;
import cy.agorise.graphenej.api.android.NetworkServiceManager;

/**
 * Sample application class
 */

public class SampleApplication extends Application {
    private final String TAG = this.getClass().getName();

    @Override
    public void onCreate() {
        super.onCreate();

        // Specifying some important information regarding the connection, such as the
        // credentials and the requested API accesses
        int requestedApis = ApiAccess.API_DATABASE | ApiAccess.API_HISTORY | ApiAccess.API_NETWORK_BROADCAST;
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(NetworkService.KEY_USERNAME, "nelson")
                .putString(NetworkService.KEY_PASSWORD, "secret")
                .putInt(NetworkService.KEY_REQUESTED_APIS, requestedApis)
                .apply();

        /*
        * Registering this class as a listener to all activity's callback cycle events, in order to
        * better estimate when the user has left the app and it is safe to disconnect the websocket connection
        */
        registerActivityLifecycleCallbacks(new NetworkServiceManager(this));
    }
}
