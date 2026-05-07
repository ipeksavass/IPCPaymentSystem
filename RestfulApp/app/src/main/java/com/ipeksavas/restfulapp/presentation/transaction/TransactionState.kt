package com.ipeksavas.restfulapp.presentation.transaction

import com.ipeksavas.restfulapp.domain.model.Receipt

data class TransactionState(
    val searchQuery: String = "",
    val transactions: List<Receipt> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTransaction: Receipt? = null,
    val showRefundConfirmDialog: Boolean = false,
    val message: String? = null // User notifications
)
