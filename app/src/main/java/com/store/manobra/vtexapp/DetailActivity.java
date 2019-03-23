package com.store.manobra.vtexapp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends Activity {
    private SessionInfo mSessionInfo;
    private OrderInfo mOrderInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        Intent i = getIntent();
        mSessionInfo = (SessionInfo) i.getSerializableExtra("sessionInfo");
        mOrderInfo = (OrderInfo) i.getSerializableExtra("orderInfo");

        TextView labelStoreName = findViewById(R.id.label_nome_loja);
        TextView labelStoreAddr = findViewById(R.id.label_end_loja);

        ImageView qr = findViewById(R.id.img_qr);
        qr.setVisibility(View.VISIBLE);

        if (mSessionInfo.isStaff) {
            labelStoreName.setText("Detalhes do pedido");
            labelStoreAddr.setText("");
            qr.setVisibility(View.INVISIBLE);
        }

        else {
            labelStoreName.setText(mOrderInfo.getStoreNameText());
            labelStoreAddr.setText(mOrderInfo.getStoreAddressText());

            QRCodeWriter writer = new QRCodeWriter();
            try {
                BitMatrix bitMatrix = writer.encode(mOrderInfo.code, BarcodeFormat.QR_CODE, 512, 512);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                qr.setImageBitmap(bmp);
            } catch (Exception e) {
            }
        }

        TextView labelName = findViewById(R.id.label_nome_cliente);
        labelName.setText(mOrderInfo.getNameText());

        TextView labelStatus = findViewById(R.id.label_status_data);
        labelStatus.setText(mOrderInfo.getStatusText());

        TextView labelDesc = findViewById(R.id.label_desc);
        labelDesc.setText(mOrderInfo.getDescText(!mSessionInfo.isStaff));

        TextView labelPayment = findViewById(R.id.label_preço_pag);
        labelPayment.setText(mOrderInfo.getPaymentText());

        TextView labelSeller = findViewById(R.id.label_nome_vend);
        labelSeller.setText(mOrderInfo.getSellerText());
    }
}
