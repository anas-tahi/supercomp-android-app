package com.supercomp.android.ui.screens.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.supercomp.android.data.model.Product
import com.supercomp.android.data.remote.RetrofitClient
import com.supercomp.android.ui.components.BottomBar
import com.supercomp.android.ui.components.ProductImage
import com.supercomp.android.ui.components.SupermarketLogo
import com.supercomp.android.ui.screens.home.supermarketColor
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

class FavoritesViewModel : ViewModel() {

    private val _items     = MutableStateFlow<List<Pair<String, Product>>>(emptyList())
    val items: StateFlow<List<Pair<String, Product>>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun load(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val wishlistResponse = RetrofitClient.api.getWishlistByUser(userId)
                if (!wishlistResponse.isSuccessful) return@launch

                val wishlistItems = wishlistResponse.body() ?: emptyList()

                val productsResponse = RetrofitClient.api.getAllProducts()
                val allProducts = if (productsResponse.isSuccessful)
                    productsResponse.body() ?: emptyList()
                else emptyList()

                val paired = wishlistItems.mapNotNull { entry ->
                    val productId = extractId(entry.productId)
                    if (productId.isBlank()) return@mapNotNull null
                    val product = allProducts.find { it.id == productId }
                        ?: return@mapNotNull null
                    Pair(entry.id, product)
                }
                _items.value = paired
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun remove(wishlistId: String, userId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.removeFromWishlist(wishlistId)
                load(userId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun extractId(productId: Any): String {
        return try {
            when (productId) {
                is String    -> productId
                is Map<*, *> -> productId["\$oid"]?.toString()
                    ?: productId["_id"]?.toString()
                    ?: productId["id"]?.toString()
                    ?: ""
                else -> productId.toString()
            }
        } catch (e: Exception) { "" }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun FavoritesScreen(
    navController: NavController,
    username: String,
    userId: String,
    viewModel: FavoritesViewModel = viewModel()
) {
    val items     by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(userId) { viewModel.load(userId) }

    Scaffold(
        bottomBar      = { BottomBar(navController, username, userId) },
        containerColor = SuperNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Favourites ❤️", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = SuperTextPrimary)
            Text("Your saved products", style = MaterialTheme.typography.bodyMedium,
                color = SuperTextSecond)
            Spacer(Modifier.height(4.dp))
            Text("Tap ❤️ on any product to add it here.",
                style = MaterialTheme.typography.bodySmall, color = SuperTextSecond)
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator(color = SuperGreen)
                }
                items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No favourites yet.", fontWeight = FontWeight.SemiBold,
                            color = SuperTextPrimary)
                        Text("Tap the heart on any product to save it.",
                            color = SuperTextSecond)
                    }
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { (wishlistId, product) ->
                        FavouriteProductCard(
                            product  = product,
                            onRemove = { viewModel.remove(wishlistId, userId) }
                        )
                    }
                }
            }
        }
    }
}

// ── Card with image ───────────────────────────────────────────────────────────

@Composable
fun FavouriteProductCard(product: Product, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
        border   = BorderStroke(1.dp, SuperBorder)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product photo with supermarket logo badge
            ProductImage(
                imageUrl     = product.imageUrl,
                supermarket  = product.supermarket,
                size         = 52.dp,
                cornerRadius = 10.dp,
                badgeSize    = 18.dp
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, color = SuperTextPrimary)
                Spacer(Modifier.height(3.dp))
                // Supermarket badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = supermarketColor(product.supermarket).copy(alpha = 0.15f)
                ) {
                    Text(
                        product.supermarket,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize   = 11.sp,
                        color      = supermarketColor(product.supermarket),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("€${"%.2f".format(product.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    color      = SuperGreen,
                    fontSize   = 17.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null,
                        tint = SuperRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick  = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove",
                            tint     = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}