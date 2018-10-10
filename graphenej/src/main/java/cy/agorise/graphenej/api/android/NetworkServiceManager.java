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
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class should be instantiated at the application level of the android app.
 *
 * It will monitor the interaction between the different activities of an app and help us decide
 * when the connection to the full node should be interrupted.
 */

public class NetworkServiceManager implements Application.ActivityLifecycleCallbacks  {
    private final static String TAG = "NetworkServiceManager";
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

    // Attributes that might need to be passed to the NetworkService
    private String mUserName = "";
    private String mPassword = "";
    private int mRequestedApis;
    private List<String> mCustomNodeUrls = new ArrayList<>();
    private boolean mAutoConnect;
    private boolean mVerifyLatency;

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
    public void onActivityCreated(Activity activity, Bundle bundle) { }

    @Override
    public void onActivityStarted(Activity activity) { }

    @Override
    public void onActivityResumed(Activity activity) {
        mHandler.removeCallbacks(mDisconnectRunnable);
        if(mService == null){
            // Creating a new Intent that will be used to start the NetworkService
            Context context = mContextReference.get();
            Intent intent = new Intent(context, NetworkService.class);

            // Adding user-provided node URLs
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<String> it = mCustomNodeUrls.iterator();
            while(it.hasNext()){
                stringBuilder.append(it.next());
                if(it.hasNext()) stringBuilder.append(",");
            }
            String customNodes = stringBuilder.toString();

            // Adding all
            intent.putExtra(NetworkService.KEY_USERNAME, mUserName)
                    .putExtra(NetworkService.KEY_PASSWORD, mPassword)
                    .putExtra(NetworkService.KEY_REQUESTED_APIS, mRequestedApis)
                    .putExtra(NetworkService.KEY_CUSTOM_NODE_URLS, customNodes)
                    .putExtra(NetworkService.KEY_AUTO_CONNECT, mAutoConnect)
                    .putExtra(NetworkService.KEY_ENABLE_LATENCY_VERIFIER, mVerifyLatency);
            context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

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

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public int getRequestedApis() {
        return mRequestedApis;
    }

    public void setRequestedApis(int mRequestedApis) {
        this.mRequestedApis = mRequestedApis;
    }

    public List<String> getCustomNodeUrls() {
        return mCustomNodeUrls;
    }

    public void setCustomNodeUrls(List<String> mCustomNodeUrls) {
        this.mCustomNodeUrls = mCustomNodeUrls;
    }

    public boolean isAutoConnect() {
        return mAutoConnect;
    }

    public void setAutoConnect(boolean mAutoConnect) {
        this.mAutoConnect = mAutoConnect;
    }

    public boolean isVerifyLatency() {
        return mVerifyLatency;
    }

    public void setVerifyLatency(boolean mVerifyLatency) {
        this.mVerifyLatency = mVerifyLatency;
    }

    /**
     * Class used to create a {@link NetworkServiceManager} with specific attributes.
     */
    public static class Builder {
        private String username;
        private String password;
        private int requestedApis;
        private List<String> customNodeUrls;
        private boolean autoconnect = true;
        private boolean verifyNodeLatency;

        /**
         * Sets the user name, if required to connect to a node.
         * @param name  User name
         * @return      The Builder instance
         */
        public Builder setUserName(String name){
            this.username = name;
            return this;
        }

        /**
         * Sets the password, if required to connect to a node.
         * @param password  Password
         * @return          The Builder instance
         */
        public Builder setPassword(String password){
            this.password = password;
            return this;
        }

        /**
         * Sets an integer with the requested APIs encoded as binary flags.
         * @param apis  Integer representing the different APIs we require from the node.
         * @return      The Builder instance
         */
        public Builder setRequestedApis(int apis){
            this.requestedApis = apis;
            return this;
        }

        /**
         * Adds a list of custom node URLs.
         * @param nodeUrls  List of custom full node URLs.
         * @return          The Builder instance
         */
        public Builder setCustomNodeUrls(List<String> nodeUrls){
            this.customNodeUrls = nodeUrls;
            return this;
        }

        /**
         * Adds a list of custom node URLs.
         * @param nodeUrls  List of custom full node URLs.
         * @return          The Builder instance
         */
        public Builder setCustomNodeUrls(String nodeUrls){
            String[] urls = nodeUrls.split(",");
            for(String url : urls){
                if(customNodeUrls == null) customNodeUrls = new ArrayList<>();
                customNodeUrls.add(url);
            }
            return this;
        }

        /**
         * Sets the autoconnect flag. This is true by default.
         * @param autoConnect   True if we want the service to connect automatically, false otherwise.
         * @return              The Builder instance
         */
        public Builder setAutoConnect(boolean autoConnect){
            this.autoconnect = autoConnect;
            return this;
        }

        /**
         * Sets the node-verification flag. This is false by default.
         * @param verifyLatency True if we want the service to perform a latency analysis before connecting.
         * @return              The Builder instance.
         */
        public Builder setNodeLatencyVerification(boolean verifyLatency){
            this.verifyNodeLatency = verifyLatency;
            return this;
        }

        /**
         * Method used to build a {@link NetworkServiceManager} instance with all of the characteristics
         * passed as parameters.
         * @param context   A Context of the application package implementing
         * this class.
         * @return          Instance of the NetworkServiceManager class.
         */
        public NetworkServiceManager build(Context context){
            NetworkServiceManager manager = new NetworkServiceManager(context);
            if(username != null) manager.setUserName(username); else manager.setUserName("");
            if(password != null) manager.setPassword(password); else manager.setPassword("");
            if(customNodeUrls != null) manager.setCustomNodeUrls(customNodeUrls);
            manager.setRequestedApis(requestedApis);
            manager.setAutoConnect(autoconnect);
            manager.setVerifyLatency(verifyNodeLatency);
            return manager;
        }
    }
}
