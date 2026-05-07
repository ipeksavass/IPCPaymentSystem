package com.ipeksavas.restfulapp.domain.model
data class Product(
    val productId: Int,
    val name: String,
    val price: Double,
    val departmentId: Int,
    val departmentName: String
)
data class SaleItem(
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val departmentId: Int,
    val departmentName: String
)
data class Receipt(
    val receiptId: Int? = null,
    val receiptDateTime: String,
    val totalAmount: Double,
    val paymentType: String,
    val items: List<SaleItem>
)
