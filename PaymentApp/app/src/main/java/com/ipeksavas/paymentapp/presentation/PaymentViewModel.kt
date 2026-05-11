package com.ipeksavas.paymentapp.presentation

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipeksavas.paymentapp.ipc.AdminIpcClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val ipcClient: AdminIpcClient
) : ViewModel() {

    init {
        ipcClient.connectToAdmin()
    }

    fun approvePayment(receiptId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = ipcClient.sendPaymentApproval(receiptId)
            if (success) {
                playSuccessTone()
                Log.d("PaymentVM", "Ödeme onayı gönderildi. ID : $receiptId")
            } else {
                Log.d("PaymentVM", "Ödeme onayı gönderilemedi. ID : $receiptId")
            }
            
            delay(1000) 
            onComplete(success)
        }
    }
    
    fun playSuccessTone() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 500)
        } catch (e: Exception) {
            Log.e("PaymentVM", "Tone error", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ipcClient.disconnect()
    }
}