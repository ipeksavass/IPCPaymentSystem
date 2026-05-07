package com.ipeksavas.adminservice.model;

import java.util.List;

public class IncomingReceiptDTO {
    public int receiptId;
    public String status;
    public String receiptDateTime;
    public double totalAmount;
    public String paymentType;
    public List<SaleItemEntity> items;
}