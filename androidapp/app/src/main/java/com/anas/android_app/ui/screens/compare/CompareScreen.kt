package com.anas.android_app.ui.screens.compare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CompareScreen(
    vm: CompareViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Compare Prices", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = vm::loadAll,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Text("READ – Load All Products")
        }

        Spacer(Modifier.height(8.dp))

        Text("FILTER by supermarket:", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mercadona", "Lidl", "Dia").forEach { name ->
                OutlinedButton(
                    onClick = { vm.filterBy(name) },
                    enabled = !state.loading
                ) { Text(name) }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.loading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }

        state.status?.let {
            Text(it, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(product.name, fontWeight = FontWeight.Medium)
                        Text(
                            "${product.supermarket}  ·  €${"%.2f".format(product.price)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}