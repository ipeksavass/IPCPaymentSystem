package com.ipeksavas.paymentapp.presentation

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import com.ipeksavas.paymentapp.ipc.AdminIpcClient

class PaymentViewModel(
    private val ipcClient: AdminIpcClient
) : ViewModel() {

    init {
        ipcClient.connectToAdmin()
    }

    fun approvePayment(receiptId: Int, onComplete: () -> Unit) {
        
        playSuccessTone()

        val success = ipcClient.sendPaymentApproval(receiptId)
        if (success) {
            onComplete()
        } else {
            onComplete()
        }
    }
    
    fun playSuccessTone() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 500)
    }

    override fun onCleared() {
        super.onCleared()
        ipcClient.disconnect()
    }
}