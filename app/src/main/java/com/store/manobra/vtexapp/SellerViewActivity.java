package com.store.manobra.vtexapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SellerViewActivity extends Activity {
    private SessionInfo mSessionInfo;

    private ProgressDialog mProgressDialog;

    private class CodeInfoCallback implements RequestTask.Callback {
        private SellerViewActivity mActivity;

        CodeInfoCallback(SellerViewActivity activity) {
            mActivity = activity;
        }

        public void onSuccess(JSONObject result) {
            mActivity.handleCodeInfo(result, null);
        }

        public void onError(Exception e) {
            mActivity.handleCodeInfo(null, e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_view);

        Intent i = getIntent();
        mSessionInfo = (SessionInfo) i.getSerializableExtra("sessionInfo");

        TextView storeNameLabel = findViewById(R.id.label_nome_loja);
        storeNameLabel.setText(mSessionInfo.storeName);

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

        Button listButton = findViewById(R.id.button_seller_list);
        listButton.setTag(this);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent((Activity)v.getTag(), ListSellerActivity.class);
                i.putExtra("sessionInfo", mSessionInfo);
                startActivity(i);
            }
        });

        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setTag(this);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SellerViewActivity activity = (SellerViewActivity) v.getTag();
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CAMERA}, 1);
                }
                else {
                    activity.openScanner();
                }

            }
        });
    }

    public void openScanner() {
        Intent i = new Intent(getApplicationContext(), ScannerActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openScanner();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String code = data.getStringExtra("code");
            if (code != null && code.length() > 0) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(getString(R.string.gui_label_login_progress));
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                CodeInfoCallback callback = new CodeInfoCallback(this);
                RequestTask task = new RequestTask(getString(R.string.api_code_info),
                                            "session=" + mSessionInfo.sessionToken +
                                                    "&code=" + code, callback);
                task.execute();
            }
        }
    }

    private void handleCodeInfo(JSONObject result, Exception e) {
        mProgressDialog.dismiss();
        mProgressDialog = null;

        try {
            if (result != null && result.getBoolean("success")) {
                OrderInfo order = new OrderInfo();
                JSONObject orderObj = result.getJSONObject("data");
                order.code = orderObj.getString("code");
                order.name = orderObj.getString("name");
                order.seller = orderObj.getString("seller");
                order.price = orderObj.getDouble("price");
                order.status = orderObj.getInt("status");
                order.expectedDate = orderObj.getString("expected_date");
                order.paymentInfo = orderObj.getString("payment_info");
                order.desc = orderObj.getString("desc");

                Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                i.putExtra("sessionInfo", mSessionInfo);
                i.putExtra("orderInfo", order);
                startActivityForResult(i, 1);
            }
        }
        catch (JSONException error) {
        }
    }
}
