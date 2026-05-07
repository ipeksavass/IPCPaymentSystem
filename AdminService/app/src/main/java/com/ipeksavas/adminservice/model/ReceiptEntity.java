package com.ipeksavas.adminservice.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipts")
public class ReceiptEntity {
    @PrimaryKey
    public int receiptId;
    public String receiptDateTime;
    public double totalAmount;
    public String paymentType;
    public String status; // "WAITING" or "SUCCESSFUL"
}