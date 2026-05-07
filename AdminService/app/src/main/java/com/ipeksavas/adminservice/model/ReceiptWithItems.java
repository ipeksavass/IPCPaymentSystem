package com.ipeksavas.adminservice.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;
public class ReceiptWithItems {
    @Embedded
    public ReceiptEntity receipt;
    @Relation(
            parentColumn = "receiptId",
            entityColumn = "parentReceiptId"
    )
    public List<SaleItemEntity> items;
}