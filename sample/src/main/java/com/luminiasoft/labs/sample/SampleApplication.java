package com.luminiasoft.labs.sample;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import cy.agorise.graphenej.api.ApiAccess;

/**
 * Sample application class
 */

public class SampleApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private final String TAG = this.getClass().getName();

    /**
     * Handler instance used to schedule tasks back to the main thread
     */
    private Handler mHandler = new Handler();

    /**
     * Constant used to specify how long will the app wait for another activity to go through its starting life
     * cycle events before running the teardownConnectionTask task.
     *
     * This is used as a means to detect whether or not the user has left the app.
     */
    private final int DISCONNECT_DELAY = 1500;

    /**
     * Runnable used to schedule a service disconnection once the app is not visible to the user for
     * more than DISCONNECT_DELAY milliseconds.
     */
    private final Runnable mDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"Runing stopService");
            stopService(new Intent(getApplicationContext(), NetworkService.class));
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, NetworkService.class);
        int requestedApis = ApiAccess.API_DATABASE | ApiAccess.API_HISTORY | ApiAccess.API_NETWORK_BROADCAST;
        intent.putExtra(NetworkService.KEY_REQUESTED_APIS, requestedApis);
        startService(intent);

        /*
        * Registering this class as a listener to all activity's callback cycle events, in order to
        * better estimate when the user has left the app and it is safe to disconnect the websocket connection
        */
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        mHandler.removeCallbacks(mDisconnectRunnable);
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
        mHandler.postDelayed(mDisconnectRunnable, DISCONNECT_DELAY);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
