package com.ipeksavas.adminservice.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.ipeksavas.adminservice.model.ReceiptEntity;
import com.ipeksavas.adminservice.model.ReceiptWithItems;
import com.ipeksavas.adminservice.model.SaleItemEntity;
import java.util.List;

@Dao
public interface PaymentDao {
    @Insert
    long insertReceipt(ReceiptEntity receipt);

    @Insert
    void insertSaleItems(List<SaleItemEntity> items);

    @Transaction
    @Query("SELECT * FROM receipts WHERE receiptDateTime LIKE :date || '%'")
    List<ReceiptWithItems> getTransactionsByDate(String date);

    @Query("DELETE FROM receipts WHERE receiptId = :id")
    void deleteReceiptById(int id);

    @Query("UPDATE receipts SET status = :newStatus WHERE receiptId = :id")
    void updateReceiptStatus(int id, String newStatus);
    
}