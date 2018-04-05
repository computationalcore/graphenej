package cy.agorise.graphenej.api.android;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import java.lang.ref.WeakReference;

/**
 * This class should be instantiated at the application level of the android app.
 *
 * It will monitor the interaction between the different activities of an app and help us decide
 * when the connection to the full node should be interrupted.
 */

public class NetworkServiceManager implements Application.ActivityLifecycleCallbacks  {
    private final String TAG = this.getClass().getName();

    /**
     * Constant used to specify how long will the app wait for another activity to go through its starting life
     * cycle events before running the teardownConnectionTask task.
     *
     * This is used as a means to detect whether or not the user has left the app.
     */
    private final int DISCONNECT_DELAY = 1500;

    /**
     * Handler instance used to schedule tasks back to the main thread
     */
    private Handler mHandler = new Handler();

    /**
     * Weak reference to the application context
     */
    private WeakReference<Context> mContextReference;

    // In case we want to interact directly with the service
    private NetworkService mService;

    /**
     * Runnable used to schedule a service disconnection once the app is not visible to the user for
     * more than DISCONNECT_DELAY milliseconds.
     */
    private final Runnable mDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = mContextReference.get();
            if(mService != null){
                context.unbindService(mServiceConnection);
                mService = null;
            }
            context.stopService(new Intent(context, NetworkService.class));
        }
    };

    public NetworkServiceManager(Context context){
        mContextReference = new WeakReference<Context>(context);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if(mService == null){
            // Starting a NetworkService instance
            Context context = mContextReference.get();
            Intent intent = new Intent(context, NetworkService.class);
            context.startService(intent);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mHandler.removeCallbacks(mDisconnectRunnable);
        if(mService == null){
            Context context = mContextReference.get();
            Intent intent = new Intent(context, NetworkService.class);
            context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {
        mHandler.postDelayed(mDisconnectRunnable, DISCONNECT_DELAY);
    }

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    /** Defines callbacks for backend binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };
}
