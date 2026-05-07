package com.ipeksavas.restfulapp.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppConstants {
    // IPC Message Types
    const val MSG_SEND_PAYMENT = 1
    const val MSG_PAYMENT_RESPONSE = 2
    const val MSG_GET_TRANSACTIONS = 3
    const val MSG_REFUND_REQUEST = 4

    // JSON Keys
    const val KEY_PAYMENT_DATA = "payment_data"
    const val KEY_RESPONSE_DATA = "response_data"
    const val PAYMENT_CASH = "CASH"
    const val PAYMENT_CREDIT_CARD = "CREDIT_CARD"

    // App B (AdminService) Package Informations
    const val ADMIN_SERVICE_PACKAGE = "com.ipeksavas.adminservice"
    const val ADMIN_SERVICE_CLASS = "com.ipeksavas.adminservice.service.AdminIpcService"
}

object DateUtils {
    fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}