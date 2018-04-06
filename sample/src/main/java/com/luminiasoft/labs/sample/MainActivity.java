package com.luminiasoft.labs.sample;

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

import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cy.agorise.graphenej.api.ConnectionStatusUpdate;
import cy.agorise.graphenej.api.android.NetworkService;
import cy.agorise.graphenej.api.android.RxBus;
import cy.agorise.graphenej.api.calls.GetBlock;
import cy.agorise.graphenej.models.JsonRpcResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    @BindView(R.id.connection_status)
    TextView mConnectionStatus;

    @BindView(R.id.response)
    TextView mResponse;

    // In case we want to interact directly with the service
    private NetworkService mService;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RxBus.getBusInstance()
            .asFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object message) throws Exception {
                if(message instanceof String){
                    Log.d(TAG,"Got text message: "+(message));
                    mResponse.setText(mResponse.getText() + ((String) message) + "\n");
                }else if(message instanceof ConnectionStatusUpdate){
                    Log.d(TAG,"Got connection update. Status: "+((ConnectionStatusUpdate)message).getConnectionStatus());
                    mConnectionStatus.setText(((ConnectionStatusUpdate) message).getConnectionStatus());
                }else if(message instanceof JsonRpcResponse){
                    mResponse.setText(mResponse.getText() + gson.toJson(message, JsonRpcResponse.class) + "\n");
                }
            }
        });
    }

    @OnClick(R.id.send_message)
    public void onSendMesage(View v){
        GetBlock getBlock = new GetBlock(1000000);
        mService.sendMessage(getBlock, GetBlock.REQUIRED_API);
    }

    @OnClick(R.id.next_activity)
    public void onNextActivity(View v){
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
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
