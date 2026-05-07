package com.ipeksavas.adminservice.service;

public final class IpcProtocol {
    public static final int MSG_SEND_PAYMENT = 1;
    public static final int MSG_PAYMENT_RESPONSE = 2;
    public static final int MSG_GET_TRANSACTIONS = 3;
    public static final int MSG_REFUND_REQUEST = 4;
    public static final int MSG_UPDATE_PAYMENT_STATUS = 5;
    
    
    public static final String KEY_PAYMENT_DATA = "payment_data";
    public static final String KEY_RESPONSE_DATA = "response_data";
    public static final String KEY_QUERY_DATE = "query_date";
    
    private IpcProtocol() { }
}
