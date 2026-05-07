package com.ipeksavas.restfulapp.presentation.cart

import com.ipeksavas.restfulapp.domain.model.SaleItem
data class CartState(
    val cartItems: List<SaleItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val paymentSuccessMessage: String? = null,
    val errorMessage: String? = null
)
