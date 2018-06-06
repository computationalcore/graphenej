package cy.agorise.graphenej.api.android;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;

import cy.agorise.graphenej.AccountOptions;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.Authority;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.api.calls.GetAccounts;
import cy.agorise.graphenej.api.calls.GetBlock;
import cy.agorise.graphenej.api.calls.GetRequiredFees;
import cy.agorise.graphenej.models.Block;
import cy.agorise.graphenej.operations.CustomOperation;
import cy.agorise.graphenej.operations.LimitOrderCreateOperation;
import cy.agorise.graphenej.operations.TransferOperation;

/**
 * Class used to store a mapping of request class to two important things:
 *
 * 1- The class to which the corresponding response should be de-serialized to
 * 2- An instance of the Gson class, with all required type adapters
 */
public class DeserializationMap {
    private final String TAG = this.getClass().getName();

    private HashMap<Class, Class> mClassMap = new HashMap<>();

    private HashMap<Class, Gson> mGsonMap = new HashMap<>();

    public DeserializationMap(){
        // GetBlock
        mClassMap.put(GetBlock.class, Block.class);
        Gson getBlockGson = new GsonBuilder()
                .registerTypeAdapter(Transaction.class, new Transaction.TransactionDeserializer())
                .registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer())
                .registerTypeAdapter(LimitOrderCreateOperation.class, new LimitOrderCreateOperation.LimitOrderCreateDeserializer())
                .registerTypeAdapter(CustomOperation.class, new CustomOperation.CustomOperationDeserializer())
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .create();
        mGsonMap.put(GetBlock.class, getBlockGson);

        // GetAccounts
        mClassMap.put(GetAccounts.class, List.class);
        Gson getAccountsGson = new GsonBuilder()
                .setExclusionStrategies(new GetAccountsExclusionStrategy())
                .registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer())
                .registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer())
                .create();
        mGsonMap.put(GetAccounts.class, getAccountsGson);

        // GetRequiredFees
        mClassMap.put(GetRequiredFees.class, List.class);
        Gson getRequiredFeesGson = new GsonBuilder()
                .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                .create();
        mGsonMap.put(GetRequiredFees.class, getRequiredFeesGson);
    }

    public Class getReceivedClass(Class _class){
        return mClassMap.get(_class);
    }

    public Gson getGson(Class aClass) {
        return mGsonMap.get(aClass);
    }

    /**
     * This class is required in order to break a recursion loop when de-serializing the
     * AccountProperties class instance.
     */
    private class GetAccountsExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == AccountOptions.class;
        }
    }
}
