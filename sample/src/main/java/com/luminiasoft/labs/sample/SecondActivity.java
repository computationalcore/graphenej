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

import com.google.common.primitives.UnsignedLong;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.api.android.NetworkService;
import cy.agorise.graphenej.api.android.RxBus;
import cy.agorise.graphenej.api.calls.GetRequiredFees;
import cy.agorise.graphenej.models.JsonRpcResponse;
import cy.agorise.graphenej.operations.LimitOrderCreateOperation;
import cy.agorise.graphenej.operations.TransferOperation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SecondActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

    @BindView(R.id.text_field)
    TextView mTextField;

    // In case we want to interact directly with the service
    private NetworkService mService;

    private Gson gson = new Gson();

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.d(TAG,"onCreate");

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
                        }else if(message instanceof JsonRpcResponse){
                            mTextField.setText(mTextField.getText() + gson.toJson(message, JsonRpcResponse.class) + "\n");
                        }
                    }
                });
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

    @OnClick(R.id.transfer_fee_usd)
    public void onTransferFeeUsdClicked(View v){
        List<BaseOperation> operations = getTransferOperation();
        mService.sendMessage(new GetRequiredFees(operations, new Asset("1.3.121")), GetRequiredFees.REQUIRED_API);
    }

    @OnClick(R.id.transfer_fee_bts)
    public void onTransferFeeBtsClicked(View v){
        List<BaseOperation> operations = getTransferOperation();
        mService.sendMessage(new GetRequiredFees(operations, new Asset("1.3.0")), GetRequiredFees.REQUIRED_API);
    }

    @OnClick(R.id.exchange_fee_usd)
    public void onExchangeFeeUsdClicked(View v){
        List<BaseOperation> operations = getExchangeOperation();
        mService.sendMessage(new GetRequiredFees(operations, new Asset("1.3.121")), GetRequiredFees.REQUIRED_API);
    }

    @OnClick(R.id.exchange_fee_bts)
    public void onExchangeFeeBtsClicked(View v){
        List<BaseOperation> operations = getExchangeOperation();
        mService.sendMessage(new GetRequiredFees(operations, new Asset("1.3.0")), GetRequiredFees.REQUIRED_API);
    }

    private List<BaseOperation> getTransferOperation(){
        TransferOperation transferOperation = new TransferOperation(
                new UserAccount("1.2.138632"),
                new UserAccount("1.2.129848"),
                new AssetAmount(UnsignedLong.ONE, new Asset("1.3.0")));
        ArrayList<BaseOperation> operations = new ArrayList();
        operations.add(transferOperation);
        return operations;
    }

    public List<BaseOperation> getExchangeOperation() {
        LimitOrderCreateOperation operation = new LimitOrderCreateOperation(
                new UserAccount("1.2.138632"),
                new AssetAmount(UnsignedLong.valueOf(10000), new Asset("1.3.0")),
                new AssetAmount(UnsignedLong.valueOf(10), new Asset("1.3.121")),
                1000000,
                true);
        ArrayList<BaseOperation> operations = new ArrayList();
        operations.add(operation);
        return operations;
    }
}
