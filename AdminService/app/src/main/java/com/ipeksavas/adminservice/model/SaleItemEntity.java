package com.ipeksavas.adminservice.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sale_items",
        foreignKeys = @ForeignKey(
                entity = ReceiptEntity.class,
                parentColumns = "receiptId",
                childColumns = "parentReceiptId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentReceiptId")}
)
public class SaleItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int parentReceiptId; // The relationship ID that identifies which record it belongs to
    public int productId;

    public String name;
    public double price;
    public int quantity;
    public int departmentId;
    public String departmentName;
}
