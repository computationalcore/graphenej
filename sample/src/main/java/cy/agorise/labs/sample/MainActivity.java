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
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ConnectionStatusUpdate;
import cy.agorise.graphenej.api.android.DeserializationMap;
import cy.agorise.graphenej.api.android.NetworkService;
import cy.agorise.graphenej.api.android.RxBus;
import cy.agorise.graphenej.api.calls.GetAccounts;
import cy.agorise.graphenej.api.calls.GetBlock;
import cy.agorise.graphenej.api.calls.GetBlockHeader;
import cy.agorise.graphenej.api.calls.GetRelativeAccountHistory;
import cy.agorise.graphenej.api.calls.LookupAssetSymbols;
import cy.agorise.graphenej.models.JsonRpcResponse;
import cy.agorise.graphenej.objects.Memo;
import cy.agorise.graphenej.operations.TransferOperation;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    @BindView(R.id.connection_status)
    TextView mConnectionStatus;

    @BindView(R.id.response)
    TextView mResponse;

    @BindView(R.id.argument_get_accounts)
    EditText mArgumentGetAccounts;

    @BindView(R.id.argument_get_block)
    EditText mArgumentGetBlock;

    @BindView(R.id.argument_get_block_header)
    EditText mArgumentGetBlockHeader;

    @BindView(R.id.argument_get_relative_account_history)
    EditText mArgumentGetRelativeAccountHistory;

    @BindView(R.id.argument_lookup_asset_symbol)
    EditText mLookupAssetSymbol;

    // In case we want to interact directly with the service
    private NetworkService mService;

    private Gson gson = new GsonBuilder()
            .setExclusionStrategies(new DeserializationMap.SkipAccountOptionsStrategy(), new DeserializationMap.SkipAssetOptionsStrategy())
            .registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer())
            .registerTypeAdapter(Memo.class, new Memo.MemoSerializer())
            .create();

    private Disposable mDisposable;

    private HashMap<Long, Integer> responseMap = new HashMap<>();

    private final int GET_ACCOUNTS_RESPONSE = 0;
    private final int GET_BLOCK_RESPONSE = 1;
    private final int GET_BLOCK_HEADER_RESPONSE = 2;
    private final int GET_RELATIVE_ACCOUNT_HISTORY_RESPONSE = 3;
    private final int LOOKUP_ASSET_SYMBOL = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDisposable = RxBus.getBusInstance()
            .asFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object message) throws Exception {
                if(message instanceof String){
                    mResponse.setText(mResponse.getText() + ((String) message) + "\n");
                    handleTextMessage((String) message);
                }else if(message instanceof ConnectionStatusUpdate){
                    mConnectionStatus.setText(((ConnectionStatusUpdate) message).getConnectionStatus());
                }else if(message instanceof JsonRpcResponse){
                    handleJsonRpcResponse((JsonRpcResponse) message);
                }
            }
        });
    }

    private void handleTextMessage(String text){

    }

    /**
     * Internal method that will decide what to do with each JSON-RPC response
     *
     * @param response The JSON-RPC api call response
     */
    private void handleJsonRpcResponse(JsonRpcResponse response){
        long id = response.id;
        if(responseMap.get(id) != null){
            int responseId = responseMap.get(id);
            switch(responseId){
                case GET_BLOCK_RESPONSE:
                    mResponse.setText(mResponse.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
                    break;
                case GET_ACCOUNTS_RESPONSE:
                    mResponse.setText(mResponse.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
                    break;
                case GET_RELATIVE_ACCOUNT_HISTORY_RESPONSE:
                    mResponse.setText(mResponse.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
                    break;
                case GET_BLOCK_HEADER_RESPONSE:
                    mResponse.setText(mResponse.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
                    break;
                case LOOKUP_ASSET_SYMBOL:
                    mResponse.setText(mResponse.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
                    break;
                default:
                    Log.w(TAG,"Case not handled");
            }
            // Remember to remove the used id entry from the map, as it would
            // otherwise just increase the app's memory usage
            responseMap.remove(id);
        }else{
            Log.d(TAG,"No entry");
            mResponse.setText(mResponse.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
        }
    }

    @OnClick(R.id.call_get_block)
    public void onGetBlock(View v){
        String str = mArgumentGetBlock.getText().toString();
        try{
            Long blockNum = Long.parseLong(str);
            GetBlock getBlock = new GetBlock(blockNum);
            long id = mService.sendMessage(getBlock, GetBlock.REQUIRED_API);
            // Registering the used sequence id
            responseMap.put(id, GET_BLOCK_RESPONSE);
        }catch(NumberFormatException e){
            Log.e(TAG,"NumberFormatException. Msg: "+e.getMessage());
        }
    }

    @OnClick(R.id.call_get_block_header)
    public void onGetBlockHeader(View v){
        String str = mArgumentGetBlockHeader.getText().toString();
        try{
            Long blockNum = Long.parseLong(str);
            GetBlockHeader getBlockHeader = new GetBlockHeader(blockNum);
            long id = mService.sendMessage(getBlockHeader, GetBlockHeader.REQUIRED_API);
            // Registering the used sequence id
            responseMap.put(id, GET_BLOCK_HEADER_RESPONSE);
        }catch(NumberFormatException e){
            Log.e(TAG,"NumberFormatException. Msg: "+e.getMessage());
        }
    }

    @OnClick(R.id.call_get_accounts)
    public void onGetAccounts(View v){
        String userId = mArgumentGetAccounts.getText().toString();
        GetAccounts getAccounts = new GetAccounts(new UserAccount(userId));
        long id = mService.sendMessage(getAccounts, GetBlock.REQUIRED_API);
        // Registering the used sequence id
        responseMap.put(id, GET_ACCOUNTS_RESPONSE);
    }

    @OnClick(R.id.call_get_relative_account_history)
    public void onGetRelativeAccountHistory(View v){
        String userId = mArgumentGetRelativeAccountHistory.getText().toString();
        UserAccount userAccount = new UserAccount(userId);
        GetRelativeAccountHistory getRelativeAccountHistory = new GetRelativeAccountHistory(userAccount, 0, 20, 0);
        long id = mService.sendMessage(getRelativeAccountHistory, GetRelativeAccountHistory.REQUIRED_API);
        responseMap.put(id, GET_RELATIVE_ACCOUNT_HISTORY_RESPONSE);
    }

    @OnClick(R.id.call_lookup_asset_symbol)
    public void onLookupAssetSymbol(View v){
        String assetId = mLookupAssetSymbol.getText().toString();
        Asset asset = new Asset(assetId);
        long id = mService.sendMessage(new LookupAssetSymbols(asset), LookupAssetSymbols.REQUIRED_API);
        responseMap.put(id, LOOKUP_ASSET_SYMBOL);
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
}
