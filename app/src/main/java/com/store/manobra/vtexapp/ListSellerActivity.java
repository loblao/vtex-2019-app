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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListSellerActivity extends Activity {
    private SessionInfo mSessionInfo;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListSellerAdapter mAdapter;

    private ProgressDialog mProgressDialog;

    private class ListCallback implements RequestTask.Callback {
        private ListSellerActivity mActivity;

        ListCallback(ListSellerActivity activity) {
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
        setContentView(R.layout.activity_list_seller);

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

        mAdapter = new ListSellerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        TextView loggedInAsLabel = findViewById(R.id.label_logged_in_as);
        loggedInAsLabel.setText("Logado como " + mSessionInfo.username + "\n(" + mSessionInfo.storeName + ")");

        Button logoutButton = findViewById(R.id.button_logout);
        logoutButton.setTag(this);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)v.getTag()).finish();
            }
        });

        ListCallback callback = new ListCallback(this);
        RequestTask task = new RequestTask(getString(R.string.api_seller_list_endpoint),
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
                        order.seller = orderObj.getString("seller");
                        order.price = orderObj.getDouble("price");
                        order.status = orderObj.getInt("status");
                        order.expectedDate = orderObj.getString("expected_date");
                        order.paymentInfo = orderObj.getString("payment_info");
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

        Log.e("will quit", e.toString(), e);
        finish();
    }

    private class ListSellerAdapter extends RecyclerView.Adapter<ListSellerAdapter.ListSellerViewHolder> {
        private List<OrderInfo> mDataset;

        public class ListSellerViewHolder extends RecyclerView.ViewHolder {
            public OrderInfo mInfo;
            public TextView mLabelStore;
            public TextView mLabelStatus;
            public TextView mLabelName;
            public LinearLayout mItem;

            public ListSellerViewHolder(View v) {
                super(v);
                mLabelStore = v.findViewById(R.id.label_store);
                mLabelStatus = v.findViewById(R.id.label_status);
                mLabelName = v.findViewById(R.id.label_name);
                mItem = v.findViewById(R.id.item);
                mItem.setTag(this);
                mItem.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ListSellerViewHolder vh = (ListSellerViewHolder) v.getTag();
                        vh.open();
                    }
                });
            }

            public void open() {
                Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                i.putExtra("sessionInfo", mSessionInfo);
                i.putExtra("orderInfo", mInfo);
                startActivity(i);
            }
        }

        public ListSellerAdapter() {
        }

        @Override
        public ListSellerAdapter.ListSellerViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_list_item, parent, false);

            ListSellerViewHolder vh = new ListSellerViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ListSellerViewHolder holder, int position) {
            holder.mInfo = mDataset.get(position);
            holder.mLabelStore.setText(holder.mInfo.name);
            holder.mLabelName.setText(holder.mInfo.getPaymentText());
            holder.mLabelStatus.setText(holder.mInfo.getStatusText());
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
