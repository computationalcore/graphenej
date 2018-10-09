package cy.agorise.labs.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cy.agorise.graphenej.RPC;

public class CallsActivity extends AppCompatActivity {

    @BindView(R.id.call_list)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);
        ButterKnife.bind(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new CallAdapter());
    }

    private final class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {

        private String[] supportedCalls = new String[]{
            RPC.CALL_GET_OBJECTS,
            RPC.CALL_GET_ACCOUNTS,
            RPC.CALL_GET_BLOCK,
            RPC.CALL_GET_BLOCK_HEADER,
            RPC.CALL_GET_MARKET_HISTORY,
            RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY,
            RPC.CALL_GET_REQUIRED_FEES,
            RPC.CALL_LOOKUP_ASSET_SYMBOLS,
            RPC.CALL_LIST_ASSETS,
            RPC.CALL_GET_ACCOUNT_BY_NAME,
            RPC.CALL_GET_LIMIT_ORDERS,
            RPC.CALL_GET_ACCOUNT_HISTORY_BY_OPERATIONS,
            RPC.CALL_GET_FULL_ACCOUNTS,
            RPC.CALL_SET_SUBSCRIBE_CALLBACK,
            RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES,
            RPC.CALL_GET_KEY_REFERENCES
        };

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = supportedCalls[position];
            String formattedName = name.replace("_", " ").toUpperCase();
            holder.mCallNameView.setText(formattedName);
            holder.mCallNameView.setOnClickListener((view) -> {
                String selectedCall = supportedCalls[position];
                Intent intent;
                if(selectedCall.equals(RPC.CALL_SET_SUBSCRIBE_CALLBACK)){
                    intent = new Intent(CallsActivity.this, SubscriptionActivity.class);
                }else{
                    intent = new Intent(CallsActivity.this, PerformCallActivity.class);
                    intent.putExtra(Constants.KEY_SELECTED_CALL, selectedCall);
                }
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return supportedCalls.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mCallNameView;

            public ViewHolder(TextView view) {
                super(view);
                this.mCallNameView = view;
            }
        }
    }
}
