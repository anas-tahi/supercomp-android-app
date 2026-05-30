package com.supercomp.android.ui.screens.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.supercomp.android.data.model.Product
import com.supercomp.android.data.model.Wishlist
import com.supercomp.android.data.remote.RetrofitClient
import com.supercomp.android.ui.components.BottomBar
import com.supercomp.android.ui.components.ProductImage
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel

class FavoritesViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<Pair<String, Product>>>(emptyList())
    val items: StateFlow<List<Pair<String, Product>>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun load(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch both wishlist and full product catalog in same time
                val wishlistResponse  = RetrofitClient.api.getWishlistByUser(userId)
                val productsResponse  = RetrofitClient.api.getAllProducts()

                val wishlistItems = wishlistResponse.body() ?: emptyList()
                val catalog       = productsResponse.body() ?: emptyList()

                // Matches what the user chose with what you have in our data
                _items.value = wishlistItems.mapNotNull { wishlist ->
                    val product = wishlist.resolveProduct(catalog)
                    if (product != null) Pair(wishlist.id, product) else null
                }
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Favourites ❤️", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SuperTextPrimary)
            Text("Your saved products", color = SuperTextSecond)
            Text("Tap 🗺 to find the nearest store", color = SuperTextSecond, fontSize = 11.sp)

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator(color = SuperGreen)
                }
            } else if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No favourites yet.", color = SuperTextSecond, fontSize = 16.sp)
                        Text("Tap ❤ on any product to save it.", color = SuperTextSecond, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { (wishlistId, product) ->
                        FavouriteProductCard(
                            product  = product,
                            onRemove = { viewModel.remove(wishlistId, userId) },
                            onMap    = { navController.navigate("map/${product.supermarket}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavouriteProductCard(
    product:  Product,
    onRemove: () -> Unit,
    onMap:    () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
        border   = BorderStroke(1.dp, SuperBorder)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(
                imageUrl     = product.imageUrl,
                supermarket  = product.supermarket,
                size         = 56.dp,
                cornerRadius = 10.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, color = SuperTextPrimary)
                Text(product.supermarket, fontSize = 11.sp, color = SuperTextSecond)
                Text(
                    "€${"%.2f".format(product.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    color      = SuperGreen,
                    fontSize   = 15.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onMap, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = SuperGreen, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}