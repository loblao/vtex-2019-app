package com.store.manobra.vtexapp;

import android.text.Html;
import android.text.Spanned;

import java.io.Serializable;

public class OrderInfo implements Serializable {
    String name;
    String code;
    String store;
    String storeAddr;
    double price;
    int status;
    String expectedDate;
    String paymentInfo;
    String seller;
    String link;
    String desc;

    public Spanned getStoreNameText() {
        return Html.fromHtml(store);
    }

    public Spanned getStoreAddressText() {
        return Html.fromHtml(String.format("<a href=\"#\">%s</a>", storeAddr));
    }

    public Spanned getNameText() {
        return Html.fromHtml("<b>O produto será retirado por:</b> " + name);
    }

    public Spanned getSellerText() {
        if (seller.length() == 0)
            return Html.fromHtml("");
        return Html.fromHtml("<b>O produto será entregue por:</b> " + seller);
    }

    public Spanned getStatusText() {
        String statusText = "<b>Status:</b> ";
        switch (status) {
            case 0:
                statusText += "Pendente";
                break;
            case 1:
                statusText += "Previsão de chegada: " + expectedDate;
                break;
            case 2:
                statusText += "Pronto para retirada desde " + expectedDate;
                break;
        }

        return Html.fromHtml(statusText);
    }

    public Spanned getPaymentText() {
        String paymentText = "<b>Valor:</b> ";
        paymentText += String.format("R$ %.2f (%s)", price, paymentInfo);
        return Html.fromHtml(paymentText);
    }

    public Spanned getDescText(Boolean putLink) {
        String descText = "<b>Descrição:</b> " + desc;
        if (putLink)
            descText += String.format(" <a href=\"%s\">Ver mais</a>", link);
        return Html.fromHtml(descText);
    }
}
