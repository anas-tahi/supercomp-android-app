package com.supercomp.android.ui.screens.shoppinglist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.supercomp.android.data.model.ShoppingListRequest
import com.supercomp.android.data.remote.RetrofitClient
import com.supercomp.android.ui.components.BottomBar
import com.supercomp.android.ui.components.ProductImage
import com.supercomp.android.ui.screens.home.supermarketColor
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val SUPERMARKETS = listOf("Mercadona", "Lidl", "Carrefour", "Alcampo")

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ShoppingListViewModel : ViewModel() {

    private val _allProducts    = MutableStateFlow<List<Product>>(emptyList())
    private val _searchQuery    = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults  = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults

    private val _basket         = MutableStateFlow<List<Product>>(emptyList())
    val basket: StateFlow<List<Product>> = _basket

    private val _comparison     = MutableStateFlow<Map<String, Double>>(emptyMap())
    val comparison: StateFlow<Map<String, Double>> = _comparison

    private val _isLoading      = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _savedLists     = MutableStateFlow<List<com.supercomp.android.data.model.ShoppingList>>(emptyList())
    val savedLists: StateFlow<List<com.supercomp.android.data.model.ShoppingList>> = _savedLists

    init { loadAllProducts() }

    private fun loadAllProducts() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getAllProducts()
                if (r.isSuccessful) _allProducts.value = r.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        val lower   = query.lowercase()
        val grouped = _allProducts.value
            .filter { it.name.lowercase().contains(lower) }
            .groupBy { it.name.lowercase().trim() }
            .values.map { it.first() }
        _searchResults.value = grouped
    }

    fun addToBasket(productName: String) {
        val productsForName  = _allProducts.value.filter {
            it.name.lowercase().trim() == productName.lowercase().trim()
        }
        val existingNames = _basket.value.map { it.name.lowercase().trim() }.toSet()
        if (!existingNames.contains(productName.lowercase().trim())) {
            _basket.value = _basket.value + productsForName
            recalculate()
        }
        _searchResults.value = emptyList()
        _searchQuery.value   = ""
    }

    fun removeFromBasket(productName: String) {
        _basket.value = _basket.value.filter {
            it.name.lowercase().trim() != productName.lowercase().trim()
        }
        recalculate()
    }

    private fun recalculate() {
        val basketByName = _basket.value.groupBy { it.name.lowercase().trim() }
        val totals       = mutableMapOf<String, Double>()
        SUPERMARKETS.forEach { market ->
            var total        = 0.0
            var canCalculate = true
            basketByName.forEach { (_, productsForName) ->
                val p = productsForName.find { it.supermarket == market }
                if (p != null) total += p.price else canCalculate = false
            }
            if (canCalculate && basketByName.isNotEmpty()) totals[market] = total
        }
        _comparison.value = totals
    }

    fun basketProductNames(): List<String> = _basket.value.map { it.name }.distinct()

    // representative product per name (for image display)
    fun representativeProduct(name: String): Product? =
        _basket.value.firstOrNull { it.name == name }

    fun saveList(userId: String, name: String) {
        val productIds = _basket.value.map { it.id }.distinct()
        viewModelScope.launch {
            try {
                RetrofitClient.api.createShoppingList(ShoppingListRequest(userId, name, productIds))
                loadSavedLists(userId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadSavedLists(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val r = RetrofitClient.api.getShoppingListsByUser(userId)
                if (r.isSuccessful) _savedLists.value = r.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun deleteSavedList(id: String, userId: String) {
        viewModelScope.launch {
            try { RetrofitClient.api.deleteShoppingList(id); loadSavedLists(userId) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun clearBasket() { _basket.value = emptyList(); _comparison.value = emptyMap() }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ShoppingListScreen(
    navController: NavController,
    username: String,
    userId: String,
    viewModel: ShoppingListViewModel = viewModel()
) {
    val basket        by viewModel.basket.collectAsState()
    val comparison    by viewModel.comparison.collectAsState()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val savedLists    by viewModel.savedLists.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var selectedTab    by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId) { viewModel.loadSavedLists(userId) }

    if (showSaveDialog) {
        SaveListDialog(
            onDismiss = { showSaveDialog = false },
            onSave    = { name -> viewModel.saveList(userId, name); showSaveDialog = false }
        )
    }

    Scaffold(
        bottomBar      = { BottomBar(navController, username, userId) },
        containerColor = SuperNavy
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) {
                Text("Shopping List Compare 🛒", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = SuperTextPrimary)
                Text("Add products and compare totals across supermarkets",
                    style = MaterialTheme.typography.bodyMedium, color = SuperTextSecond)
            }

            Spacer(Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = SuperSurface,
                contentColor     = SuperGreen
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Build & Compare") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Saved Lists") })
            }

            when (selectedTab) {
                0 -> BuildCompareTab(
                    searchQuery   = searchQuery,
                    searchResults = searchResults,
                    basketNames   = viewModel.basketProductNames(),
                    comparison    = comparison,
                    getRepProduct = { viewModel.representativeProduct(it) },
                    onSearch      = { viewModel.search(it) },
                    onAdd         = { viewModel.addToBasket(it) },
                    onRemove      = { viewModel.removeFromBasket(it) },
                    onSave        = { showSaveDialog = true },
                    onClear       = { viewModel.clearBasket() }
                )
                1 -> SavedListsTab(
                    savedLists = savedLists,
                    isLoading  = isLoading,
                    onDelete   = { viewModel.deleteSavedList(it, userId) }
                )
            }
        }
    }
}

// ── Build & Compare Tab ───────────────────────────────────────────────────────

@Composable
fun BuildCompareTab(
    searchQuery: String,
    searchResults: List<Product>,
    basketNames: List<String>,
    comparison: Map<String, Double>,
    getRepProduct: (String) -> Product?,
    onSearch: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    val cheapest = comparison.minByOrNull { it.value }?.key

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(12.dp)) }

        // Search bar
        item {
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = onSearch,
                placeholder   = { Text("Search: leche, pan, agua...") },
                shape         = RoundedCornerShape(12.dp),
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                leadingIcon   = { Icon(Icons.Filled.Search, contentDescription = null) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SuperGreen,
                    unfocusedBorderColor = SuperBorder,
                    focusedTextColor     = SuperTextPrimary,
                    unfocusedTextColor   = SuperTextPrimary,
                    cursorColor          = SuperGreen
                )
            )
        }

        // Search results with image
        if (searchResults.isNotEmpty()) {
            items(searchResults) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
                    border   = BorderStroke(1.dp, SuperBorder)
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductImage(
                            imageUrl     = product.imageUrl,
                            supermarket  = product.supermarket,
                            size         = 44.dp,
                            cornerRadius = 8.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold,
                                color = SuperTextPrimary)
                            Text("Available in ${SUPERMARKETS.size} supermarkets",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuperTextSecond)
                        }
                        IconButton(onClick = { onAdd(product.name) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = SuperGreen)
                        }
                    }
                }
            }
        }

        // Basket header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your Basket", fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp, color = SuperTextPrimary)
                Spacer(Modifier.weight(1f))
                if (basketNames.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (basketNames.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                    Text("Search and add products above to compare totals.",
                        color = SuperTextSecond)
                }
            }
        } else {
            // Basket items with image
            items(basketNames) { name ->
                val rep = getRepProduct(name)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
                    border   = BorderStroke(1.dp, SuperBorder)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (rep != null) {
                            ProductImage(
                                imageUrl     = rep.imageUrl,
                                supermarket  = rep.supermarket,
                                size         = 40.dp,
                                cornerRadius = 8.dp
                            )
                            Spacer(Modifier.width(10.dp))
                        } else {
                            Text("🛒", fontSize = 20.sp)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(name, fontWeight = FontWeight.Medium,
                            color = SuperTextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemove(name) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Comparison totals
            item {
                Spacer(Modifier.height(8.dp))
                Text("Total by Supermarket", fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp, color = SuperTextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Based on ${basketNames.size} product(s)",
                    style = MaterialTheme.typography.bodySmall, color = SuperTextSecond)
                Spacer(Modifier.height(8.dp))
            }

            if (comparison.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = SuperSurface)
                    ) {
                        Text("Not all products are available in every supermarket.",
                            modifier = Modifier.padding(16.dp), color = SuperTextSecond)
                    }
                }
            } else {
                items(comparison.entries.sortedBy { it.value }) { (market, total) ->
                    val isBest = market == cheapest
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = if (isBest) SuperGreen.copy(0.12f) else SuperSurface2
                        ),
                        border   = BorderStroke(1.dp,
                            if (isBest) SuperGreen.copy(0.5f) else SuperBorder),
                        elevation = CardDefaults.cardElevation(if (isBest) 4.dp else 1.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(CircleShape)
                                    .background(supermarketColor(market)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(market.take(1), color = Color.White,
                                    fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(market,
                                fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                                color      = SuperTextPrimary, modifier = Modifier.weight(1f))
                            Text("€${"%.2f".format(total)}",
                                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                                color = if (isBest) SuperGreen else SuperTextPrimary)
                            if (isBest) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SuperGreen.copy(0.2f)
                                ) {
                                    Text("Cheapest",
                                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        color      = SuperGreen, fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick  = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = SuperGreen)
                    ) {
                        Text("Save this list", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Saved Lists Tab ───────────────────────────────────────────────────────────

@Composable
fun SavedListsTab(
    savedLists: List<com.supercomp.android.data.model.ShoppingList>,
    isLoading: Boolean,
    onDelete: (String) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = SuperGreen)
        }
        return
    }
    if (savedLists.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🗂️", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("No saved lists.", fontWeight = FontWeight.SemiBold, color = SuperTextPrimary)
                Text("Build a list and tap Save.", color = SuperTextSecond)
            }
        }
        return
    }
    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(savedLists) { list ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
                border   = BorderStroke(1.dp, SuperBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(list.name, fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = SuperTextPrimary,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDelete(list.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    // Show product images in a row
                    if (list.products.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            list.products.distinctBy { it.name }.take(5).forEach { p ->
                                ProductImage(
                                    imageUrl     = p.imageUrl,
                                    supermarket  = p.supermarket,
                                    size         = 36.dp,
                                    cornerRadius = 6.dp,
                                    fallbackFontSize = 14.sp
                                )
                            }
                            if (list.products.distinctBy { it.name }.size > 5) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                                        .background(SuperSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+${list.products.distinctBy { it.name }.size - 5}",
                                        fontSize = 11.sp, color = SuperTextSecond,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        list.products.distinctBy { it.name }.forEach { p ->
                            Text("• ${p.name}", style = MaterialTheme.typography.bodyMedium,
                                color = SuperTextSecond)
                        }
                    }
                }
            }
        }
    }
}

// ── Save Dialog ───────────────────────────────────────────────────────────────

@Composable
fun SaveListDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save this list") },
        text  = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("List name") }, singleLine = true,
                shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick  = { onSave(name.trim()) },
                enabled  = name.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = SuperGreen)
            ) { Text("Save", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}