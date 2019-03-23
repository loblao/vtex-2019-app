package com.store.manobra.vtexapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListUserActivity extends Activity {
    private SessionInfo mSessionInfo;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListUserAdapter mAdapter;

    private ProgressDialog mProgressDialog;

    private class ListCallback implements RequestTask.Callback {
        private ListUserActivity mActivity;

        ListCallback(ListUserActivity activity) {
            mActivity = activity;
        }

        public void onSuccess(JSONObject result) {
            mActivity.handleList(result, null);
        }

        public void onError(Exception e) {
            mActivity.handleList(null, e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        Intent i = getIntent();
        mSessionInfo = (SessionInfo) i.getSerializableExtra("sessionInfo");

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.gui_label_loading));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ListUserAdapter();
        mRecyclerView.setAdapter(mAdapter);

        TextView loggedInAsLabel = findViewById(R.id.label_logged_in_as);
        loggedInAsLabel.setText("Logado como " + mSessionInfo.username);

        Button logoutButton = findViewById(R.id.button_logout);
        logoutButton.setTag(this);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)v.getTag()).finish();
            }
        });

        ListCallback callback = new ListCallback(this);
        RequestTask task = new RequestTask(getString(R.string.api_user_list_endpoint),
                                           "session=" + mSessionInfo.sessionToken,
                                            callback);
        task.execute();
    }

    private void handleList(JSONObject result, Exception e) {
        mProgressDialog.dismiss();
        mProgressDialog = null;

        try {
            if (result != null) {
                if (result.getBoolean("success")) {
                    List<OrderInfo> dataset = new ArrayList<>();

                    JSONArray array = result.getJSONArray("data");
                    Log.i("huh", array.toString());
                    for (int i = 0; i < array.length(); i++) {
                        OrderInfo order = new OrderInfo();
                        JSONObject orderObj = array.getJSONObject(i);
                        order.code = orderObj.getString("code");
                        order.name = orderObj.getString("name");
                        order.store = orderObj.getJSONObject("store").getString("name");
                        order.store_addr = orderObj.getJSONObject("store").getString("addr");
                        order.seller = orderObj.getJSONObject("store").getString("seller");
                        order.price = orderObj.getDouble("price");
                        order.status = orderObj.getInt("status");
                        order.expected_date = orderObj.getString("expected_date");
                        order.payment_info = orderObj.getString("payment_info");
                        order.link = orderObj.getString("link");
                        order.desc = orderObj.getString("desc");
                        dataset.add(order);
                    }

                    mAdapter.setDataset(dataset);
                    return;
                }

                else {
                    e = new Exception("result is null");
                }
            }
        }

        catch (JSONException error) {
            e = error;
        }

        finish();
    }

    private class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ListUserViewHolder> {
        private List<OrderInfo> mDataset;

        public class ListUserViewHolder extends RecyclerView.ViewHolder {
            public OrderInfo mInfo;
            public TextView mLabelStore;
            public TextView mLabelStatus;
            public TextView mLabelName;
            public Button mButton;

            public ListUserViewHolder(View v) {
                super(v);
                mLabelStore = v.findViewById(R.id.label_store);
                mLabelStatus = v.findViewById(R.id.label_status);
                mLabelName = v.findViewById(R.id.label_name);
               /* mButton = v.findViewById(R.id.button);
                mButton.setTag(this);
                mButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ListUserViewHolder vh = (ListUserViewHolder) v.getTag();
                        Log.i("lulz", vh.mInfo.code);
                    }
                });*/
            }
        }

        public ListUserAdapter() {
        }

        @Override
        public ListUserAdapter.ListUserViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_list_item, parent, false);

            ListUserViewHolder vh = new ListUserViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ListUserViewHolder holder, int position) {
            holder.mInfo = mDataset.get(position);
            holder.mLabelStore.setText(holder.mInfo.store);
            holder.mLabelName.setText("O produto será retirado por " + holder.mInfo.name);

            String statusText = "";
            switch (holder.mInfo.status) {
                case 0:
                    statusText = "Pendente";
                    break;
                case 1:
                    statusText = "Previsão de chegada: " + holder.mInfo.expected_date;
                    break;
                case 2:
                    statusText = "Pronto para retirada desde " + holder.mInfo.expected_date;
                    break;
            }

            holder.mLabelStatus.setText(statusText);
        }

        @Override
        public int getItemCount() {
            return mDataset != null ? mDataset.size() : 0;
        }

        public void setDataset(List<OrderInfo> dataset) {
            mDataset = dataset;
            notifyDataSetChanged();
        }
    }
}
