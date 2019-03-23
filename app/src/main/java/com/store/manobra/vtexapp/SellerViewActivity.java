package com.store.manobra.vtexapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SellerViewActivity extends Activity {
    private SessionInfo mSessionInfo;

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
    }
}
