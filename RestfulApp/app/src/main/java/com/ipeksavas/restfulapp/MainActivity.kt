package com.ipeksavas.restfulapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ipeksavas.restfulapp.domain.repository.PaymentRepositoryImpl
import com.ipeksavas.restfulapp.presentation.cart.CartScreen
import com.ipeksavas.restfulapp.presentation.cart.CartViewModel
import com.ipeksavas.restfulapp.presentation.transaction.TransactionScreen
import com.ipeksavas.restfulapp.presentation.transaction.TransactionViewModel
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = PaymentRepositoryImpl(applicationContext)
        val cartViewModel = CartViewModel(repository)
        val transactionViewModel = TransactionViewModel(repository)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "cart") {
                    composable("cart") {
                        CartScreen(
                            viewModel = cartViewModel,
                            onNavigateToTransactions = { navController.navigate("transactions") }
                        )
                    }
                    composable("transactions") {
                        TransactionScreen(
                            viewModel = transactionViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}