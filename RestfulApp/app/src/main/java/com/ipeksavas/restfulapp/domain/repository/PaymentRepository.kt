package com.ipeksavas.restfulapp.domain.repository

import com.ipeksavas.restfulapp.domain.model.Receipt

interface PaymentRepository {
    suspend fun sendPayment(receipt: Receipt): Result<Boolean>
    suspend fun getTransactionsByDate(date: String): Result<List<Receipt>>
    suspend fun refundTransaction(receiptId: Int): Result<Boolean>
}