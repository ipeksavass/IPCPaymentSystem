package com.ipeksavas.restfulapp.presentation.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ipeksavas.restfulapp.domain.model.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Geçmiş İşlemler") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Tarih Sorgula (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.fetchTransactions() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(state.transactions) { receipt ->
                        TransactionItem(receipt = receipt) {
                            viewModel.onTransactionClick(receipt)
                        }
                    }
                }
            }
        }

        state.selectedTransaction?.let { receipt ->
            AlertDialog(
                onDismissRequest = { viewModel.closeDetail() },
                title = { Text("İşlem Detayı (ID: ${receipt.receiptId})") },
                text = {
                    Column {
                        Text("Tarih: ${receipt.receiptDateTime}")
                        Text("Tutar: ${receipt.totalAmount} TL")
                        Text("Ödeme: ${receipt.paymentType}")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        receipt.items.forEach { item ->
                            Text("${item.name} x ${item.quantity} = ${item.price * item.quantity} TL")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.requestRefund() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("İade Et") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeDetail() }) { Text("Kapat") }
                }
            )
        }

        if (state.showRefundConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelRefundRequest() },
                title = { Text("İade Onayı") },
                text = { Text("Bu işlemi iade etmek istediğinize emin misiniz?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmRefund() }) { Text("Evet") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelRefundRequest() }) { Text("Hayır") }
                }
            )
        }

        state.message?.let { msg ->
            AlertDialog(
                onDismissRequest = { viewModel.clearMessage() },
                confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("Tamam") } },
                text = { Text(msg) }
            )
        }
    }
}

@Composable
fun TransactionItem(receipt: Receipt, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("İşlem ID: ${receipt.receiptId}", fontWeight = FontWeight.Bold)
                Text("${receipt.totalAmount} TL")
            }
            Text(receipt.receiptDateTime, style = MaterialTheme.typography.bodySmall)
        }
    }
}