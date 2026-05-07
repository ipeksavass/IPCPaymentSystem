package com.ipeksavas.adminservice.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.ipeksavas.adminservice.model.ReceiptEntity;
import com.ipeksavas.adminservice.model.SaleItemEntity;

@Database(entities = {ReceiptEntity.class, SaleItemEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PaymentDao paymentDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "admin_payment_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}