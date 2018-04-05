package com.luminiasoft.labs.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.api.android.NetworkService;

public class SecondActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

    // In case we want to interact directly with the service
    private NetworkService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        int requestedApis = ApiAccess.API_DATABASE | ApiAccess.API_HISTORY | ApiAccess.API_NETWORK_BROADCAST;
        intent.putExtra(NetworkService.KEY_REQUESTED_APIS, requestedApis);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    /** Defines callbacks for backend binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG,"onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG,"onServiceDisconnected");
        }
    };
}
