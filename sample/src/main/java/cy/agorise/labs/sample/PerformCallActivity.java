package cy.agorise.labs.sample;

import android.content.ComponentName;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cy.agorise.graphenej.Memo;
import cy.agorise.graphenej.OperationType;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ConnectionStatusUpdate;
import cy.agorise.graphenej.api.android.DeserializationMap;
import cy.agorise.graphenej.api.android.RxBus;
import cy.agorise.graphenej.api.calls.GetAccountByName;
import cy.agorise.graphenej.api.calls.GetAccountHistoryByOperations;
import cy.agorise.graphenej.api.calls.GetAccounts;
import cy.agorise.graphenej.api.calls.GetBlock;
import cy.agorise.graphenej.api.calls.GetDynamicGlobalProperties;
import cy.agorise.graphenej.api.calls.GetFullAccounts;
import cy.agorise.graphenej.api.calls.GetKeyReferences;
import cy.agorise.graphenej.api.calls.GetLimitOrders;
import cy.agorise.graphenej.api.calls.GetObjects;
import cy.agorise.graphenej.api.calls.ListAssets;
import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.models.JsonRpcResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PerformCallActivity extends ConnectedActivity {
    private final String TAG = this.getClass().getName();

    @BindView(R.id.response)
    TextView mResponseView;

    @BindView(R.id.container_param1)
    TextInputLayout mParam1View;

    @BindView(R.id.container_param2)
    TextInputLayout mParam2View;

    @BindView(R.id.container_param3)
    TextInputLayout mParam3View;

    @BindView(R.id.container_param4)
    TextInputLayout mParam4View;

    @BindView(R.id.param1)
    TextInputEditText param1;

    @BindView(R.id.param2)
    TextInputEditText param2;

    @BindView(R.id.param3)
    TextInputEditText param3;

    @BindView(R.id.param4)
    TextInputEditText param4;

    @BindView(R.id.button_send)
    Button mButtonSend;

    // Field used to map a request id to its type
    private HashMap<Long, String> responseMap = new HashMap<>();

    // Current request type. Ex: 'get_objects', 'get_accounts', etc
    private String mRPC;

    private Disposable mDisposable;

    private Gson gson = new GsonBuilder()
            .setExclusionStrategies(new DeserializationMap.SkipAccountOptionsStrategy(), new DeserializationMap.SkipAssetOptionsStrategy())
            .registerTypeAdapter(Memo.class, new Memo.MemoSerializer())
            .create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_call);
        ButterKnife.bind(this);

        mRPC = getIntent().getStringExtra(Constants.KEY_SELECTED_CALL);
        Log.d(TAG,"Selected call: "+mRPC);
        switch (mRPC){
            case RPC.CALL_GET_OBJECTS:
                setupGetObjects();
                break;
            case RPC.CALL_GET_ACCOUNTS:
                setupGetAccounts();
                break;
            case RPC.CALL_GET_BLOCK:
                setupGetBlock();
                break;
            case RPC.CALL_GET_BLOCK_HEADER:
                setupGetBlockHeader();
                break;
            case RPC.CALL_GET_MARKET_HISTORY:
                setupGetMarketHistory();
                break;
            case RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY:
                setupGetRelativeAccountHistory();
                break;
            case RPC.CALL_GET_REQUIRED_FEES:
                break;
            case RPC.CALL_LOOKUP_ASSET_SYMBOLS:
                setupLookupAssetSymbols();
                break;
            case RPC.CALL_LIST_ASSETS:
                setupListAssets();
                break;
            case RPC.CALL_GET_ACCOUNT_BY_NAME:
                setupAccountByName();
                break;
            case RPC.CALL_GET_ACCOUNT_HISTORY_BY_OPERATIONS:
                setupGetAccountHistoryByOperations();
                break;
            case RPC.CALL_GET_LIMIT_ORDERS:
                setupGetLimitOrders();
            case RPC.CALL_GET_FULL_ACCOUNTS:
                setupGetFullAccounts();
                break;
            case RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES:
                setupGetDynamicGlobalProperties();
                break;
            case RPC.CALL_GET_KEY_REFERENCES:
                setupGetKeyReferences();
                break;
            default:
                Log.d(TAG,"Default called");
        }

        mDisposable = RxBus.getBusInstance()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object message) throws Exception {
                        Log.d(TAG,"accept. Msg class: "+message.getClass());
                        if(message instanceof ConnectionStatusUpdate){
                            // TODO: Update UI ?
                        }else if(message instanceof JsonRpcResponse){
                            handleJsonRpcResponse((JsonRpcResponse) message);
                        }
                    }
                });
    }

    private void setupGetObjects(){
        requiredInput(1);
        mParam1View.setHint(getResources().getString(R.string.get_objects_arg1));
    }

    private void setupGetAccounts(){
        requiredInput(1);
        mParam1View.setHint(getResources().getString(R.string.get_accounts_arg1));
    }

    private void setupGetBlock(){
        requiredInput(1);
        mParam1View.setHint(getResources().getString(R.string.get_block_arg1));
    }

    private void setupGetBlockHeader(){
        requiredInput(1);
        mParam1View.setHint(getResources().getString(R.string.get_block_arg1));
    }

    private void setupGetMarketHistory(){
        requiredInput(4);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.get_market_history_arg1));
        mParam2View.setHint(resources.getString(R.string.get_market_history_arg2));
        mParam3View.setHint(resources.getString(R.string.get_market_history_arg3));
        mParam4View.setHint(resources.getString(R.string.get_market_history_arg4));
    }

    private void setupGetRelativeAccountHistory(){
        requiredInput(4);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.get_relative_account_history_arg1));
        mParam2View.setHint(resources.getString(R.string.get_relative_account_history_arg2));
        mParam3View.setHint(resources.getString(R.string.get_relative_account_history_arg3));
        mParam4View.setHint(resources.getString(R.string.get_relative_account_history_arg4));
    }

    private void setupLookupAssetSymbols(){
        requiredInput(4);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.lookup_asset_symbols_arg1));
        mParam2View.setHint(resources.getString(R.string.lookup_asset_symbols_arg2));
        mParam3View.setHint(resources.getString(R.string.lookup_asset_symbols_arg3));
        mParam4View.setHint(resources.getString(R.string.lookup_asset_symbols_arg4));
    }

    private void setupListAssets(){
        requiredInput(2);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.list_assets_arg1));
        mParam2View.setHint(resources.getString(R.string.list_assets_arg2));
        param2.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setupAccountByName(){
        requiredInput(1);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.get_accounts_by_name_arg1));
        param1.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    private void setupGetAccountHistoryByOperations(){
        requiredInput(4);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.get_account_history_by_operations_arg1));
        mParam2View.setHint(resources.getString(R.string.get_account_history_by_operations_arg2));
        mParam3View.setHint(resources.getString(R.string.get_account_history_by_operations_arg3));
        mParam4View.setHint(resources.getString(R.string.get_account_history_by_operations_arg4));

        param2.setText("0");    // Only transfer de-serialization is currently supported by the library!
        param2.setEnabled(false);
        param2.setInputType(InputType.TYPE_CLASS_NUMBER);
        param3.setInputType(InputType.TYPE_CLASS_NUMBER);
        param4.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setupGetLimitOrders(){
        requiredInput(3);
        Resources resources = getResources();
        mParam1View.setHint(resources.getString(R.string.get_limit_orders_arg1));
        mParam2View.setHint(resources.getString(R.string.get_limit_orders_arg2));
        mParam3View.setHint(resources.getString(R.string.get_limit_orders_arg3));
        param1.setInputType(InputType.TYPE_CLASS_TEXT);
        param2.setInputType(InputType.TYPE_CLASS_TEXT);
        param3.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setupGetFullAccounts(){
        requiredInput(1);
        mParam1View.setHint(getString(R.string.get_full_accounts_arg1));
        param1.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    private void setupGetDynamicGlobalProperties(){
        requiredInput(0);
    }

    private void setupGetKeyReferences(){
        requiredInput(1);
        // Test address
        param1.setText("BTS8a7XJ94u1traaLGFHw6NgpvUaxmbG4MyCcZC1hBj9HCBuMEwXP");
    }

    private void requiredInput(int inputCount){
        if(inputCount == 0){
            mParam1View.setVisibility(View.GONE);
            mParam2View.setVisibility(View.GONE);
            mParam3View.setVisibility(View.GONE);
            mParam4View.setVisibility(View.GONE);
        }else if(inputCount == 1){
            mParam1View.setVisibility(View.VISIBLE);
            mParam2View.setVisibility(View.GONE);
            mParam3View.setVisibility(View.GONE);
            mParam4View.setVisibility(View.GONE);
        }else if(inputCount == 2){
            mParam1View.setVisibility(View.VISIBLE);
            mParam2View.setVisibility(View.VISIBLE);
            mParam3View.setVisibility(View.GONE);
            mParam4View.setVisibility(View.GONE);
        }else if(inputCount == 3){
            mParam1View.setVisibility(View.VISIBLE);
            mParam2View.setVisibility(View.VISIBLE);
            mParam3View.setVisibility(View.VISIBLE);
            mParam4View.setVisibility(View.GONE);
        }else if(inputCount == 4){
            mParam1View.setVisibility(View.VISIBLE);
            mParam2View.setVisibility(View.VISIBLE);
            mParam3View.setVisibility(View.VISIBLE);
            mParam4View.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.button_send)
    public void onSendClicked(Button v){
        switch (mRPC){
            case RPC.CALL_GET_OBJECTS:
                sendGetObjectsRequest();
                break;
            case RPC.CALL_GET_ACCOUNTS:
                sendGetAccountsRequest();
                break;
            case RPC.CALL_GET_BLOCK:
                break;
            case RPC.CALL_GET_BLOCK_HEADER:
                break;
            case RPC.CALL_GET_MARKET_HISTORY:
                break;
            case RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY:
                break;
            case RPC.CALL_GET_REQUIRED_FEES:
                break;
            case RPC.CALL_LOOKUP_ASSET_SYMBOLS:
                break;
            case RPC.CALL_LIST_ASSETS:
                sendListAssets();
                break;
            case RPC.CALL_GET_ACCOUNT_BY_NAME:
                getAccountByName();
                break;
            case RPC.CALL_GET_LIMIT_ORDERS:
                getLimitOrders();
                break;
            case RPC.CALL_GET_ACCOUNT_HISTORY_BY_OPERATIONS:
                getAccountHistoryByOperations();
                break;
            case RPC.CALL_GET_FULL_ACCOUNTS:
                getFullAccounts();
                break;
            case RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES:
                getDynamicGlobalProperties();
                break;
            case RPC.CALL_GET_KEY_REFERENCES:
                getKeyReferences();
                break;
            default:
                Log.d(TAG,"Default called");
        }
    }

    private void sendGetObjectsRequest(){
        String objectId = param1.getText().toString();
        if(objectId.matches("\\d\\.\\d{1,3}\\.\\d{1,10}")){
            ArrayList<String> array = new ArrayList<>();
            array.add(objectId);
            GetObjects getObjects = new GetObjects(array);
            long id = mNetworkService.sendMessage(getObjects, GetObjects.REQUIRED_API);
            responseMap.put(id, mRPC);
        }else{
            param1.setError(getResources().getString(R.string.error_input_id));
        }
    }

    private void sendGetAccountsRequest(){
        String userId = param1.getText().toString();
        if(userId.matches("\\d\\.\\d{1,3}\\.\\d{1,10}")){
            GetAccounts getAccounts = new GetAccounts(new UserAccount(userId));
            long id = mNetworkService.sendMessage(getAccounts, GetBlock.REQUIRED_API);
            responseMap.put(id, mRPC);
        }else{
            param1.setError(getResources().getString(R.string.error_input_id));
        }
    }

    private void sendListAssets(){
        try{
            String lowerBound = param1.getText().toString();
            int limit = Integer.parseInt(param2.getText().toString());
            ListAssets listAssets = new ListAssets(lowerBound, limit);
            long id = mNetworkService.sendMessage(listAssets, ListAssets.REQUIRED_API);
            responseMap.put(id, mRPC);
        }catch(NumberFormatException e){
            Toast.makeText(this, getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"NumberFormatException while reading limit value. Msg: "+e.getMessage());
        }
    }

    private void getAccountByName(){
        String accountName = param1.getText().toString();
        long id = mNetworkService.sendMessage(new GetAccountByName(accountName), GetAccountByName.REQUIRED_API);
        responseMap.put(id, mRPC);
    }

    private void getLimitOrders(){
        String assetA = param1.getText().toString();
        String assetB = param2.getText().toString();
        try{
            int limit = Integer.parseInt(param3.getText().toString());
            long id = mNetworkService.sendMessage(new GetLimitOrders(assetA, assetB, limit), GetLimitOrders.REQUIRED_API);
        }catch(NumberFormatException e){
            Toast.makeText(this, getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"NumberFormatException while trying to read limit value. Msg: "+e.getMessage());
        }
    }

    private void getAccountHistoryByOperations(){
        try{
            String account = param1.getText().toString();
            ArrayList<OperationType> operationTypes = new ArrayList<>();
            operationTypes.add(OperationType.TRANSFER_OPERATION); // Currently restricted to transfer operations
            long start = Long.parseLong(param3.getText().toString());
            long limit = Long.parseLong(param4.getText().toString());
            long id = mNetworkService.sendMessage(new GetAccountHistoryByOperations(account, operationTypes, start, limit), GetAccountHistoryByOperations.REQUIRED_API);
            responseMap.put(id, mRPC);
        }catch(NumberFormatException e){
            Toast.makeText(this, getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"NumberFormatException while trying to read arguments for 'get_account_history_by_operations'. Msg: "+e.getMessage());
        }
    }

    private void getFullAccounts(){
        ArrayList<String> accounts = new ArrayList<>();
        accounts.addAll(Arrays.asList(param1.getText().toString().split(",")));
        long id = mNetworkService.sendMessage(new GetFullAccounts(accounts, false), GetFullAccounts.REQUIRED_API);
        responseMap.put(id, mRPC);
    }

    private void getDynamicGlobalProperties(){
        long id = mNetworkService.sendMessage(new GetDynamicGlobalProperties(), GetDynamicGlobalProperties.REQUIRED_API);
        responseMap.put(id, mRPC);
    }

    private void getKeyReferences(){
        String address = param1.getText().toString();
        long id = 0;
        try {
            id = mNetworkService.sendMessage(new GetKeyReferences(address), GetKeyReferences.REQUIRED_API);
            responseMap.put(id, mRPC);
        } catch (MalformedAddressException | IllegalArgumentException e) {
            Log.e(TAG,"MalformedAddressException. Msg: "+e.getMessage());
            Toast.makeText(this, "Malformed address exception", Toast.LENGTH_SHORT).show();
            param1.setText("");
        }
    }

    /**
     * Internal method that will decide what to do with each JSON-RPC response
     *
     * @param response The JSON-RPC api call response
     */
    private void handleJsonRpcResponse(JsonRpcResponse response){
        long id = response.id;
        if(responseMap.get(id) != null){
            String request = responseMap.get(id);
            switch(request){
                case RPC.CALL_GET_ACCOUNTS:
                case RPC.CALL_GET_BLOCK:
                case RPC.CALL_GET_BLOCK_HEADER:
                case RPC.CALL_GET_MARKET_HISTORY:
                case RPC.CALL_GET_ACCOUNT_HISTORY:
                case RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY:
                case RPC.CALL_GET_REQUIRED_FEES:
                case RPC.CALL_LOOKUP_ASSET_SYMBOLS:
                case RPC.CALL_LIST_ASSETS:
                case RPC.CALL_GET_ACCOUNT_BY_NAME:
                case RPC.CALL_GET_LIMIT_ORDERS:
                case RPC.CALL_GET_ACCOUNT_HISTORY_BY_OPERATIONS:
                case RPC.CALL_GET_FULL_ACCOUNTS:
                case RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES:
                case RPC.CALL_GET_KEY_REFERENCES:
                    mResponseView.setText(mResponseView.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
                    break;
                default:
                    Log.w(TAG,"Case not handled");
                    mResponseView.setText(mResponseView.getText() + response.result.toString());
            }
            // Remember to remove the used id entry from the map, as it would
            // otherwise just increase the app's memory usage
            responseMap.remove(id);
        }else{
            Log.d(TAG,"No entry");
            mResponseView.setText(mResponseView.getText() + gson.toJson(response, JsonRpcResponse.class) + "\n");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!mDisposable.isDisposed())
            mDisposable.dispose();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        // Called upon NetworkService connection
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        // Called upon NetworkService disconnection
    }
}
