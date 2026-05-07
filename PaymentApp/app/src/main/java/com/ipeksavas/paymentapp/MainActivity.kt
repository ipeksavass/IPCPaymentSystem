package com.ipeksavas.paymentapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ipeksavas.paymentapp.ipc.AdminIpcClient
import com.ipeksavas.paymentapp.presentation.PaymentScreen
import com.ipeksavas.paymentapp.presentation.PaymentViewModel
import kotlin.jvm.java

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action != "com.ipeksavas.paymentapp.START_PAYMENT") {
            finish()
            return
        }

        val amount = intent.getDoubleExtra("totalAmount", 0.0)
        val receiptId = intent.getIntExtra("receiptId", -1)

        // start viewmodel via factory
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val ipcClient = AdminIpcClient(applicationContext)
                @Suppress("UNCHECKED_CAST")
                return PaymentViewModel(ipcClient) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[PaymentViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PaymentScreen(amount = amount) {
                        viewModel.approvePayment(receiptId) {
                            Toast.makeText(this, "Ödeme Onaylandı!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }
}