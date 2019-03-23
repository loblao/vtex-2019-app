package com.store.manobra.vtexapp;

import java.io.Serializable;

public class SessionInfo implements Serializable {
    public String username;
    public String sessionToken;
    public String storeName;
    public Boolean isStaff;
}
