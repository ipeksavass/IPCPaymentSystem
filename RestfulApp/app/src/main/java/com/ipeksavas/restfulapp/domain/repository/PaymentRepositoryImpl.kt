package com.ipeksavas.restfulapp.domain.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ipeksavas.restfulapp.core.AppConstants
import com.ipeksavas.restfulapp.domain.model.Receipt
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
class PaymentRepositoryImpl(private val context: Context) : PaymentRepository {
    private var targetServiceMessenger: Messenger? = null
    private val gson = Gson()
    private suspend fun getServiceMessenger(): Messenger = suspendCancellableCoroutine { continuation ->
        if (targetServiceMessenger != null) {
            continuation.resume(targetServiceMessenger!!)
            return@suspendCancellableCoroutine
        }

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d("IPC", "AdminService'e başarıyla bağlanıldı.")
                targetServiceMessenger = Messenger(service)
                continuation.resume(targetServiceMessenger!!)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d("IPC", "AdminService bağlantısı koptu.")
                targetServiceMessenger = null
            }
        }

        val intent = Intent().apply {
            setClassName(AppConstants.ADMIN_SERVICE_PACKAGE, AppConstants.ADMIN_SERVICE_CLASS)
        }

        val isBound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!isBound) {
            continuation.resumeWith(Result.failure(Exception("Servise bağlanılamadı. B uygulaması yüklü mü?")))
        }
    }

    override suspend fun sendPayment(receipt: Receipt): Result<Boolean> {
        return try {
            val service = getServiceMessenger()

            suspendCancellableCoroutine { continuation ->
                val jsonPayload = gson.toJson(receipt)

                val replyHandler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        when (msg.what) {
                            AppConstants.MSG_PAYMENT_RESPONSE -> {
                                continuation.resume(Result.success(true))
                            }
                            -1 -> {
                                continuation.resume(Result.failure(Exception("-1")))
                            }
                            else -> {
                                continuation.resume(Result.failure(Exception("Beklenmedik bir hata oluştu: ${msg.what}")))
                            }
                        }
                    }
                }

                val message = Message.obtain(null, AppConstants.MSG_SEND_PAYMENT)
                message.replyTo = Messenger(replyHandler)
                message.data = Bundle().apply {
                    putString(AppConstants.KEY_PAYMENT_DATA, jsonPayload)
                }

                service.send(message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionsByDate(date: String): Result<List<Receipt>> {
        return try {
            val service = getServiceMessenger()

            suspendCancellableCoroutine { continuation ->
                val replyHandler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        if (msg.what == AppConstants.MSG_PAYMENT_RESPONSE) {
                            val jsonResponse = msg.data.getString(AppConstants.KEY_RESPONSE_DATA)
                            if (jsonResponse != null) {
                                val listType = object : TypeToken<List<Receipt>>() {}.type
                                val receiptList: List<Receipt> = gson.fromJson(jsonResponse, listType)
                                continuation.resume(Result.success(receiptList))
                            } else {
                                continuation.resume(Result.success(emptyList()))
                            }
                        } else {
                            continuation.resume(Result.failure(Exception("Veriler alınamadı.")))
                        }
                    }
                }

                val message = Message.obtain(null, AppConstants.MSG_GET_TRANSACTIONS)
                message.replyTo = Messenger(replyHandler)
                message.data = Bundle().apply {
                    putString("query_date", date)
                }

                service.send(message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refundTransaction(receiptId: Int): Result<Boolean> {
        return try {
            val service = getServiceMessenger()

            suspendCancellableCoroutine { continuation ->
                val replyHandler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        if (msg.what == AppConstants.MSG_PAYMENT_RESPONSE) {
                            continuation.resume(Result.success(true))
                        } else {
                            continuation.resume(Result.failure(Exception("İade işlemi başarısız.")))
                        }
                    }
                }

                val message = Message.obtain(null, AppConstants.MSG_REFUND_REQUEST)
                message.replyTo = Messenger(replyHandler)
                message.data = Bundle().apply {
                    putInt("receipt_id", receiptId)
                }

                service.send(message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}