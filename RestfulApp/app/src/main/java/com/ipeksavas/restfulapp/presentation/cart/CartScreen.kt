package com.ipeksavas.restfulapp.presentation.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipeksavas.restfulapp.domain.model.Product
import com.ipeksavas.restfulapp.domain.model.ProductCatalog
import com.ipeksavas.restfulapp.core.DateUtils

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onNavigateToTransactions: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val groupedProducts = ProductCatalog.dummyProducts.groupBy { it.departmentName }

    val currentDateTime = remember { DateUtils.getCurrentDateTime() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RestfulApp - Kasa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = currentDateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(3f)
                .verticalScroll(rememberScrollState())
        ) {
            groupedProducts.forEach { (department, products) ->
                Text(
                    text = department,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onAddClick = { quantity -> viewModel.addToCart(product, quantity) },
                            onRemoveClick = { quantity -> viewModel.removeFromCart(product, quantity) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sepet Detayı",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            if (state.cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sepet boş.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.cartItems) { item ->
                        val lineTotal = item.price * item.quantity
                        Text(
                            text = "${item.name} x ${item.quantity} = ${"%.2f".format(lineTotal)} TL",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Toplam: ${"%.2f".format(state.totalAmount)} TL",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        state.paymentSuccessMessage?.let { message ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "✅ $message",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        state.errorMessage?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "⚠️ $error",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.initiatePayment(isCreditCard = false) },
                    enabled = state.cartItems.isNotEmpty() && !state.isLoading,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) { Text("Nakit") }

                Button(
                    onClick = { viewModel.initiatePayment(isCreditCard = true) },
                    enabled = state.cartItems.isNotEmpty() && !state.isLoading,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                ) { Text("Kredi Kartı") }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = onNavigateToTransactions,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) { Text("İşlemler (Transactions)") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onAddClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }

    Card(
        modifier = Modifier.width(210.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${product.price} TL",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                label = { Text("Miktar", fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        val q = quantityText.toIntOrNull() ?: 1
                        onRemoveClick(q)
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Çıkar", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        val q = quantityText.toIntOrNull() ?: 1
                        onAddClick(q)
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text("Ekle", fontSize = 11.sp)
                }
            }
        }
    }
}