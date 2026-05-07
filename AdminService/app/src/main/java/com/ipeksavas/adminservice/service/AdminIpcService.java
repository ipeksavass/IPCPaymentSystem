package com.ipeksavas.adminservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.NonNull;
import com.ipeksavas.adminservice.data.AppDatabase;
import com.ipeksavas.adminservice.model.IncomingReceiptDTO;
import com.ipeksavas.adminservice.model.ReceiptEntity;
import com.ipeksavas.adminservice.model.ReceiptWithItems;
import com.ipeksavas.adminservice.model.SaleItemEntity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminIpcService extends Service {

    private static final String TAG = "AdminService_LOG";
    private final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private Messenger globalReplyTo = null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bağlantı sağlandı.");
        return mMessenger.getBinder();
    }

    private class IncomingHandler extends Handler {
        IncomingHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "Mesaj Alındı: " + msg.what);
            switch (msg.what) {
                case IpcProtocol.MSG_SEND_PAYMENT:
                    handleNewPayment(msg);
                    break;
                case IpcProtocol.MSG_GET_TRANSACTIONS:
                    handleQueryTransactions(msg);
                    break;
                case IpcProtocol.MSG_REFUND_REQUEST:
                    handleRefund(msg);
                    break;
                case IpcProtocol.MSG_UPDATE_PAYMENT_STATUS:
                    handleCardPaymentResult(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void handleNewPayment(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null) return;
        String jsonPayload = bundle.getString(IpcProtocol.KEY_PAYMENT_DATA);
        globalReplyTo = msg.replyTo;

        executorService.execute(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
                int lastId = prefs.getInt("last_receipt_id", 0);
                int nextIdCandidate = lastId + 1;

                if (nextIdCandidate % 10 == 0) {
                    Log.e(TAG, "ID ATLATILDI: " + nextIdCandidate + " kural gereği DB'ye kaydedilmedi.");

                    prefs.edit().putInt("last_receipt_id", nextIdCandidate).apply();

                    sendResponse(globalReplyTo, -1, null);
                    return;
                }

                IncomingReceiptDTO incoming = gson.fromJson(jsonPayload, IncomingReceiptDTO.class);
                ReceiptEntity receipt = new ReceiptEntity();

                receipt.receiptId = nextIdCandidate;
                receipt.receiptDateTime = incoming.receiptDateTime;
                receipt.totalAmount = incoming.totalAmount;
                receipt.paymentType = incoming.paymentType;
                receipt.status = "WAITING";

                AppDatabase.getInstance(getApplicationContext()).paymentDao().insertReceipt(receipt);

                if (incoming.items != null) {
                    for (SaleItemEntity item : incoming.items) {
                        item.parentReceiptId = nextIdCandidate;
                    }
                    AppDatabase.getInstance(getApplicationContext()).paymentDao().insertSaleItems(incoming.items);
                }

                prefs.edit().putInt("last_receipt_id", nextIdCandidate).apply();
                Log.d(TAG, "DB Kaydı Başarılı. Yeni ID: " + nextIdCandidate);

                if ("CASH".equals(receipt.paymentType)) {
                    AppDatabase.getInstance(getApplicationContext())
                            .paymentDao().updateReceiptStatus(nextIdCandidate, "SUCCESSFUL");
                    sendResponse(globalReplyTo, IpcProtocol.MSG_PAYMENT_RESPONSE, null);
                } else {
                    Intent intent = new Intent("com.ipeksavas.paymentapp.START_PAYMENT");
                    intent.setPackage("com.ipeksavas.paymentapp");
                    intent.putExtra("totalAmount", receipt.totalAmount);
                    intent.putExtra("receiptId", nextIdCandidate);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Hata oluştu: " + e.getMessage());
            }
        });
    }

    private void handleCardPaymentResult(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null) return;

        int receiptId = bundle.getInt("receiptId");
        boolean isSuccess = bundle.getBoolean("isSuccess");

        executorService.execute(() -> {
            try {
                if (isSuccess) {
                    AppDatabase.getInstance(getApplicationContext())
                            .paymentDao().updateReceiptStatus(receiptId, "SUCCESSFUL");

                    Log.i(TAG, "C'den onay geldi. Fiş ID: " + receiptId + " SUCCESSFUL yapıldı.");

                    if (globalReplyTo != null) {
                        sendResponse(globalReplyTo, IpcProtocol.MSG_PAYMENT_RESPONSE, null);
                        globalReplyTo = null;
                    } else {
                        Log.e(TAG, "HATA: A uygulamasına dönülecek adres (globalReplyTo) bulunamadı!");
                    }

                } else {
                    Log.e(TAG, "Ödeme başarısız veya iptal edildi.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Kart sonucu işlenirken hata: " + e.getMessage());
            }
        });
    }

    private void handleQueryTransactions(Message msg) {
        Bundle bundle = msg.getData();
        String date = (bundle != null) ? bundle.getString(IpcProtocol.KEY_QUERY_DATE) : "";
        Messenger replyTo = msg.replyTo;

        executorService.execute(() -> {
            try {
                List<ReceiptWithItems> rawResults = AppDatabase.getInstance(getApplicationContext())
                        .paymentDao().getTransactionsByDate(date);

                List<IncomingReceiptDTO> responseList = new ArrayList<>();
                for (ReceiptWithItems raw : rawResults) {
                    IncomingReceiptDTO dto = new IncomingReceiptDTO();
                    dto.receiptId = raw.receipt.receiptId;
                    dto.receiptDateTime = raw.receipt.receiptDateTime;
                    dto.totalAmount = raw.receipt.totalAmount;
                    dto.paymentType = raw.receipt.paymentType;
                    dto.status = raw.receipt.status;
                    dto.items = raw.items;
                    responseList.add(dto);
                }

                String responseJson = gson.toJson(responseList);
                Bundle responseBundle = new Bundle();
                responseBundle.putString(IpcProtocol.KEY_RESPONSE_DATA, responseJson);
                sendResponse(replyTo, IpcProtocol.MSG_PAYMENT_RESPONSE, responseBundle);
            } catch (Exception e) {
                Log.e(TAG, "Sorgu hatası");
            }
        });
    }

    private void handleRefund(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null) return;
        int receiptId = bundle.getInt("receipt_id");
        Messenger replyTo = msg.replyTo;

        executorService.execute(() -> {
            try {
                AppDatabase.getInstance(getApplicationContext()).paymentDao().deleteReceiptById(receiptId);
                sendResponse(replyTo, IpcProtocol.MSG_PAYMENT_RESPONSE, null);
            } catch (Exception e) {
                Log.e(TAG, "İade hatası");
            }
        });
    }

    private void sendResponse(Messenger replyTo, int what, Bundle data) {
        if (replyTo != null) {
            try {
                Message response = Message.obtain(null, what);
                if (data != null) response.setData(data);
                replyTo.send(response);
            } catch (RemoteException e) {
                Log.e(TAG, "Cevap hatası");
            }
        }
    }
}