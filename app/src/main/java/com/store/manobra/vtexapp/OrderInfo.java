package com.store.manobra.vtexapp;

import java.io.Serializable;

public class OrderInfo implements Serializable {
    String name;
    String code;
    String store;
    String store_addr;
    double price;
    int status;
    String expected_date;
    String payment_info;
    String seller;
    String link;
    String desc;
}
