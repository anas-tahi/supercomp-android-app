package com.supercomp.android.ui.screens.compare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.supercomp.android.ui.screens.home.HomeViewModel
import com.supercomp.android.ui.screens.home.supermarketColor
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

class CompareViewModel : ViewModel() {
    private val _results   = MutableStateFlow<List<Product>>(emptyList())
    val results: StateFlow<List<Product>> = _results

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun search(query: String) {
        if (query.isBlank()) { _results.value = emptyList(); return }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.searchProducts(query)
                if (response.isSuccessful) _results.value = response.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun CompareScreen(
    navController: NavController,
    username: String,
    userId: String,
    viewModel: CompareViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val results      by viewModel.results.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val favouriteIds by homeViewModel.favouriteIds.collectAsState()
    var query        by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { homeViewModel.loadFavouriteIds(userId) }
    LaunchedEffect(query)  { viewModel.search(query) }

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
            Text("Compare Prices 🔍", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = SuperTextPrimary)
            Text("Search a product to compare across supermarkets",
                style = MaterialTheme.typography.bodyMedium, color = SuperTextSecond)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("e.g. leche, arroz, aceite...") },
                shape         = RoundedCornerShape(12.dp),
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SuperGreen,
                    unfocusedBorderColor = SuperBorder,
                    focusedTextColor     = SuperTextPrimary,
                    unfocusedTextColor   = SuperTextPrimary,
                    cursorColor          = SuperGreen
                )
            )

            Spacer(Modifier.height(16.dp))

            when {
                isLoading && results.isEmpty() ->
                    Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        CircularProgressIndicator(color = SuperGreen)
                    }
                results.isEmpty() && query.isNotBlank() && !isLoading ->
                    Text("No results for \"$query\".", color = SuperTextSecond)
                else -> {
                    val grouped = results.groupBy { it.name.lowercase().trim() }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(grouped.entries.toList()) { (_, prods) ->
                            CompareCard(
                                products     = prods,
                                favouriteIds = favouriteIds,
                                onToggleFav  = { product ->
                                    homeViewModel.toggleFavourite(userId, product, favouriteIds)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Compare Card (with images) ────────────────────────────────────────────────

@Composable
fun CompareCard(
    products: List<Product>,
    favouriteIds: Set<String>,
    onToggleFav: (Product) -> Unit
) {
    val sorted   = products.sortedBy { it.price }
    val cheapest = sorted.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
        border   = BorderStroke(1.dp, SuperBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Product header with image
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.padding(bottom = 10.dp)
            ) {
                // Product photo (shared imageUrl) with logo badge
                cheapest?.let { p ->
                    ProductImage(
                        imageUrl     = p.imageUrl,
                        supermarket  = p.supermarket,
                        size         = 64.dp,
                        cornerRadius = 12.dp,
                        badgeSize    = 22.dp
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(products.first().name, fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = SuperTextPrimary)
                    Text("${sorted.size} supermarkets compared",
                        fontSize = 11.sp, color = SuperTextSecond)
                }
            }

            HorizontalDivider(color = SuperBorder)
            Spacer(Modifier.height(8.dp))

            // Price rows per supermarket
            sorted.forEach { product ->
                val isBest = product == cheapest
                val isFav  = favouriteIds.contains(product.id)

                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small supermarket logo
                    SupermarketLogo(
                        supermarket  = product.supermarket,
                        size         = 32.dp,
                        cornerRadius = 6.dp
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        product.supermarket,
                        modifier   = Modifier.weight(1f),
                        color      = if (isBest) supermarketColor(product.supermarket)
                        else SuperTextSecond,
                        fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                        fontSize   = 13.sp
                    )

                    Text(
                        "€${"%.2f".format(product.price)}",
                        fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.Normal,
                        color      = if (isBest) SuperGreen else SuperTextPrimary,
                        fontSize   = if (isBest) 16.sp else 14.sp
                    )

                    if (isBest) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SuperGreen.copy(alpha = 0.15f)
                        ) {
                            Text("Best", color = SuperGreen, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }

                    IconButton(
                        onClick  = { onToggleFav(product) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = if (isFav) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint     = if (isFav) SuperRed else SuperTextSecond,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}