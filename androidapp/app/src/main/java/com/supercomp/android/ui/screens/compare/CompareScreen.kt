package com.supercomp.android.ui.screens.compare

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

fun openMapApp(context: Context, brand: String) {
    val query = when (brand) {
        "Mercadona" -> "Mercadona"
        "Lidl"      -> "Lidl"
        "Carrefour" -> "Carrefour"
        "Dia"       -> "Supermercados Dia"
        else        -> brand
    }
    val mapIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    val mapIntent    = Intent(Intent.ACTION_VIEW, mapIntentUri)
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
        )
        context.startActivity(webIntent)
    }
}

// ViewModel
class CompareViewModel : ViewModel() {
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _results     = MutableStateFlow<List<Product>>(emptyList())
    val results: StateFlow<List<Product>> = _results

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadAllProducts() }

    private fun loadAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.getAllProducts()
                if (response.isSuccessful) _allProducts.value = response.body() ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) { _results.value = emptyList(); return }
        val lower = query.lowercase().trim()
        _results.value = _allProducts.value.filter { it.name.lowercase().startsWith(lower) }
    }
}

// Screen

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
    val context      = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.get(0)
            if (spokenText != null) {
                query = spokenText
                viewModel.search(spokenText)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            }
            speechLauncher.launch(intent)
        }
    }

    LaunchedEffect(Unit)  { homeViewModel.loadFavouriteIds(userId) }
    LaunchedEffect(query) { viewModel.search(query) }

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
            Text(
                "Compare Prices 🔍",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = SuperTextPrimary
            )
            Text(
                "Search a product to compare across supermarkets",
                style = MaterialTheme.typography.bodyMedium,
                color = SuperTextSecond
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("e.g. leche, arroz, aceite...") },
                shape         = RoundedCornerShape(12.dp),
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                            }
                            speechLauncher.launch(intent)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = SuperGreen)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SuperGreen,
                    unfocusedBorderColor = SuperBorder,
                    focusedTextColor     = SuperTextPrimary,
                    unfocusedTextColor   = SuperTextPrimary,
                    cursorColor          = SuperGreen
                )
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading && query.isEmpty()) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator(color = SuperGreen)
                }
            } else if (results.isEmpty() && query.isNotBlank()) {
                Text("No results starting with \"$query\".", color = SuperTextSecond)
            } else {
                val displayGroups = remember(results) {
                    results.groupBy { it.name.lowercase().trim() }
                        .flatMap { (_, list) ->
                            val sortedList = list.sortedBy { it.price }
                            val clusters   = mutableListOf<MutableList<Product>>()
                            for (product in sortedList) {
                                val cluster = clusters.find { c ->
                                    val avg = c.map { it.price }.average()
                                    product.price in (avg * 0.6)..(avg * 1.4)
                                }
                                if (cluster != null) cluster.add(product)
                                else clusters.add(mutableListOf(product))
                            }
                            clusters
                        }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayGroups) { productsInCluster ->
                        CompareCard(
                            products      = productsInCluster,
                            favouriteIds  = favouriteIds,
                            onToggleFav   = { product ->
                                homeViewModel.toggleFavourite(userId, product, favouriteIds)
                            },
                            onFindNearest = { brand -> navController.navigate("map/$brand") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompareCard(
    products: List<Product>,
    favouriteIds: Set<String>,
    onToggleFav: (Product) -> Unit,
    onFindNearest: (String) -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.padding(bottom = 10.dp)
            ) {
                cheapest?.let { p ->
                    ProductImage(
                        imageUrl    = p.imageUrl,
                        supermarket = p.supermarket,
                        size        = 64.dp,
                        cornerRadius = 12.dp,
                        badgeSize   = 22.dp
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(
                        products.first().name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = SuperTextPrimary
                    )
                    Text(
                        "${sorted.size} stores compared",
                        fontSize = 11.sp,
                        color    = SuperTextSecond
                    )
                }
            }

            HorizontalDivider(color = SuperBorder)
            Spacer(Modifier.height(8.dp))

            sorted.forEach { product ->
                val isBest = product == cheapest
                val isFav  = favouriteIds.contains(product.id)

                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SupermarketLogo(supermarket = product.supermarket, size = 32.dp, cornerRadius = 6.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        product.supermarket,
                        modifier   = Modifier.weight(1f),
                        color      = if (isBest) supermarketColor(product.supermarket) else SuperTextSecond,
                        fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                        fontSize   = 13.sp
                    )
                    Text(
                        "€${"%.2f".format(product.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (isBest) SuperGreen else SuperTextPrimary,
                        fontSize   = if (isBest) 16.sp else 14.sp
                    )

                    if (isBest) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape    = RoundedCornerShape(4.dp),
                            color    = SuperGreen.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onFindNearest(product.supermarket) }
                        ) {
                            Row(
                                modifier          = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Best", color = SuperGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(2.dp))
                                Icon(Icons.Default.LocationOn, null, tint = SuperGreen, modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    IconButton(onClick = { onToggleFav(product) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
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