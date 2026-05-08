package com.ipeksavas.restfulapp.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipeksavas.restfulapp.core.AppConstants
import com.ipeksavas.restfulapp.core.DateUtils
import com.ipeksavas.restfulapp.domain.model.Product
import com.ipeksavas.restfulapp.domain.model.Receipt
import com.ipeksavas.restfulapp.domain.model.SaleItem
import com.ipeksavas.restfulapp.domain.repository.PaymentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartState())
    val uiState: StateFlow<CartState> = _uiState.asStateFlow()
    fun addToCart(product: Product, quantity: Int) {
        if (quantity <= 0) return

        val currentItems = _uiState.value.cartItems.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.productId == product.productId }

        if (existingItemIndex != -1) {
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            currentItems.add(
                SaleItem(
                    productId = product.productId,
                    name = product.name,
                    price = product.price,
                    quantity = quantity,
                    departmentId = product.departmentId,
                    departmentName = product.departmentName
                )
            )
        }

        recalculateTotal(currentItems)
    }
    fun removeFromCart(product: Product, quantity: Int) {
        if (quantity <= 0) return

        val currentItems = _uiState.value.cartItems.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.productId == product.productId }

        if (existingItemIndex != -1) {
            val existingItem = currentItems[existingItemIndex]
            val newQuantity = existingItem.quantity - quantity

            if (newQuantity > 0) {
                currentItems[existingItemIndex] = existingItem.copy(quantity = newQuantity)
            } else {
                currentItems.removeAt(existingItemIndex)
            }

            recalculateTotal(currentItems)
        }
    }
    
    fun clearCart(){
        _uiState.value = _uiState.value.copy(cartItems = emptyList())
    }

    private fun recalculateTotal(items: List<SaleItem>) {
        val newTotal = items.sumOf { it.price * it.quantity }
        _uiState.value = _uiState.value.copy(
            cartItems = items,
            totalAmount = newTotal
        )
    }

    fun initiatePayment(isCreditCard: Boolean) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return

        val receipt = Receipt(
            receiptDateTime = DateUtils.getCurrentDateTime(),
            totalAmount = currentState.totalAmount,
            paymentType = if (isCreditCard) AppConstants.PAYMENT_CREDIT_CARD else AppConstants.PAYMENT_CASH,
            items = currentState.cartItems
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, paymentSuccessMessage = null)

            val result = paymentRepository.sendPayment(receipt)

            if (result.isSuccess) {
                _uiState.value = CartState(paymentSuccessMessage = "İşlem Başarılı!")

                delay(5000)
                _uiState.value = _uiState.value.copy(paymentSuccessMessage = null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Bilinmeyen Hata"

                val userFriendlyError = if (errorMsg == "-1") {
                    "Hata: 10'un katı olan işlemler kaydedilemez!"
                } else {
                    "Ödeme sırasında bir sorun oluştu."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = userFriendlyError
                )

                delay(5000)
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }
}