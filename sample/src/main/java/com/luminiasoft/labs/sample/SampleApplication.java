package com.luminiasoft.labs.sample;

import android.app.Application;

import cy.agorise.graphenej.api.android.NetworkServiceManager;

/**
 * Sample application class
 */

public class SampleApplication extends Application {
    private final String TAG = this.getClass().getName();

    @Override
    public void onCreate() {
        super.onCreate();

        /*
        * Registering this class as a listener to all activity's callback cycle events, in order to
        * better estimate when the user has left the app and it is safe to disconnect the websocket connection
        */
        registerActivityLifecycleCallbacks(new NetworkServiceManager(this));
    }
}
