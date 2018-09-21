package cy.agorise.labs.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import cy.agorise.graphenej.api.android.NetworkService;
import cy.agorise.graphenej.network.NodeLatencyVerifier;

public abstract class ConnectedActivity extends AppCompatActivity implements ServiceConnection {
    private final String TAG = this.getClass().getName();

    /* Network service connection */
    protected NetworkService mNetworkService;

    /**
     * Flag used to keep track of the NetworkService binding state
     */
    private boolean mShouldUnbindNetwork;

    private ServiceConnection mNetworkServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            mNetworkService = binder.getService();
            ConnectedActivity.this.onServiceConnected(className, service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ConnectedActivity.this.onServiceDisconnected(componentName);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, NetworkService.class);
        // Binding to NetworkService
        if(bindService(intent, mNetworkServiceConnection, Context.BIND_AUTO_CREATE)){
            mShouldUnbindNetwork = true;
        }else{
            Log.e(TAG,"Binding to the network service failed.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unbinding from network service
        if(mShouldUnbindNetwork){
            unbindService(mNetworkServiceConnection);
            mShouldUnbindNetwork = false;
        }
    }
}
