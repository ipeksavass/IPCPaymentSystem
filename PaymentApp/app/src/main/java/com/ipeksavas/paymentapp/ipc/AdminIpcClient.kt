package com.ipeksavas.paymentapp.ipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.ipeksavas.paymentapp.ipc.PaymentState.PAYMENT_CLASS_NAME
import com.ipeksavas.paymentapp.ipc.PaymentState.PAYMENT_PACKAGE_NAME

class AdminIpcClient(private val context: Context) {

    private var adminServiceMessenger: Messenger? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            adminServiceMessenger = Messenger(service)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            adminServiceMessenger = null
            isBound = false
        }
    }

    fun connectToAdmin() {
        val intent = Intent().apply {
            setClassName(PAYMENT_PACKAGE_NAME,PAYMENT_CLASS_NAME)
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // send payment's okay message to B app
    fun sendPaymentApproval(receiptId: Int): Boolean {
        if (!isBound || adminServiceMessenger == null) return false

        return try {
            val msg = Message.obtain(null, 5)
            msg.data = Bundle().apply {
                putInt("receiptId", receiptId)
                putBoolean("isSuccess", true)
            }
            adminServiceMessenger?.send(msg)
            true
        } catch (e: RemoteException) {
            e.printStackTrace()
            false
        }
    }

    fun disconnect() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}