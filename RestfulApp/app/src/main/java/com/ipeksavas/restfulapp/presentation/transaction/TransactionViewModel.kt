package com.ipeksavas.restfulapp.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipeksavas.restfulapp.domain.model.Receipt
import com.ipeksavas.restfulapp.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionState())
    val uiState: StateFlow<TransactionState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun fetchTransactions() {
        val date = _uiState.value.searchQuery
        if (date.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = paymentRepository.getTransactionsByDate(date)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    transactions = result.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Veri çekilemedi")
            }
        }
    }

    fun onTransactionClick(receipt: Receipt) {
        _uiState.value = _uiState.value.copy(selectedTransaction = receipt)
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(selectedTransaction = null)
    }

    fun requestRefund() {
        _uiState.value = _uiState.value.copy(showRefundConfirmDialog = true)
    }

    fun cancelRefundRequest() {
        _uiState.value = _uiState.value.copy(showRefundConfirmDialog = false)
    }

    fun confirmRefund() {
        val receiptId = _uiState.value.selectedTransaction?.receiptId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showRefundConfirmDialog = false)
            val result = paymentRepository.refundTransaction(receiptId)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    message = "İade Başarılı - Lütfen Nakit İadesini Gerçekleştirin",
                    selectedTransaction = null
                )
                fetchTransactions()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "İade başarısız")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}