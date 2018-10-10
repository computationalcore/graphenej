package cy.agorise.labs.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cy.agorise.graphenej.api.android.NetworkService;
import cy.agorise.graphenej.api.android.RxBus;
import cy.agorise.graphenej.api.calls.CancelAllSubscriptions;
import cy.agorise.graphenej.api.calls.SetSubscribeCallback;
import cy.agorise.graphenej.models.JsonRpcNotification;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SubscriptionActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

    @BindView(R.id.text_field)
    TextView mTextField;

    // In case we want to interact directly with the service
    private NetworkService mService;

    private Disposable mDisposable;

    // Notification counter
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ButterKnife.bind(this);

        mDisposable = RxBus.getBusInstance()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object message) throws Exception {
                        if(message instanceof String){
                            Log.d(TAG,"Got text message: "+(message));
                            mTextField.setText(mTextField.getText() + ((String) message) + "\n");
                        }else if(message instanceof JsonRpcNotification){
                            counter++;
                            mTextField.setText(String.format("Got %d notifications so far", counter));
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
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

    @OnClick(R.id.subscribe)
    public void onTransferFeeUsdClicked(View v){
        mService.sendMessage(new SetSubscribeCallback(true), SetSubscribeCallback.REQUIRED_API);
    }

    @OnClick(R.id.unsubscribe)
    public void onTransferFeeBtsClicked(View v){
        mService.sendMessage(new CancelAllSubscriptions(), CancelAllSubscriptions.REQUIRED_API);
    }
}
