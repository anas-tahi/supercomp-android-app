package com.supercomp.android.ui.screens.shoppinglist

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.supercomp.android.data.model.Product
import com.supercomp.android.data.model.ShoppingListRequest
import com.supercomp.android.data.model.WishlistRequest
import com.supercomp.android.data.remote.RetrofitClient
import com.supercomp.android.ui.components.BottomBar
import com.supercomp.android.ui.components.ProductImage
import com.supercomp.android.ui.components.rememberVoiceToTextParser
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val SUPERMARKETS = listOf("Mercadona", "Lidl", "Carrefour", "Dia")

class ShoppingListViewModel : ViewModel() {
    private val _allProducts    = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts

    private val _searchQuery    = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults  = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults

    private val _basketNames    = MutableStateFlow<List<String>>(emptyList())
    val basketNames: StateFlow<List<String>> = _basketNames

    private val _comparison     = MutableStateFlow<Map<String, Double>>(emptyMap())
    val comparison: StateFlow<Map<String, Double>> = _comparison

    private val _showResults    = MutableStateFlow(false)
    val showResults: StateFlow<Boolean> = _showResults

    private val _isLoading      = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _savedLists     = MutableStateFlow<List<com.supercomp.android.data.model.ShoppingList>>(emptyList())
    val savedLists: StateFlow<List<com.supercomp.android.data.model.ShoppingList>> = _savedLists

    private val _error          = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favoriteIds    = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    init { loadAllProducts() }

    private fun loadAllProducts() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getAllProducts()
                if (r.isSuccessful) _allProducts.value = r.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadFavoriteIds(userId: String) {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getWishlistByUser(userId)
                if (r.isSuccessful) {
                    _favoriteIds.value = r.body()
                        ?.mapNotNull { it.getSafeProductId() }
                        ?.toSet() ?: emptySet()
                }
            } catch (_: Exception) {}
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        val lower = query.lowercase()
        _searchResults.value = _allProducts.value
            .filter { it.name.lowercase().startsWith(lower) }
            .map { it.name }.distinct().sorted()
    }

    fun addToBasket(name: String) {
        if (!_basketNames.value.contains(name))
            _basketNames.value = _basketNames.value + name
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _showResults.value = false // Hide results when basket changes
    }

    fun removeFromBasket(name: String) {
        _basketNames.value = _basketNames.value.filter { it != name }
        _showResults.value = false // Hide results when basket changes
    }

    fun performComparison() {
        if (_basketNames.value.isEmpty()) return
        val totals = mutableMapOf<String, Double>()
        SUPERMARKETS.forEach { market ->
            var sum = 0.0
            var allFound = true
            _basketNames.value.forEach { name ->
                val match = _allProducts.value.find { it.name == name && it.supermarket == market }
                if (match != null) sum += match.price else allFound = false
            }
            if (allFound) totals[market] = sum
        }
        _comparison.value = totals
        _showResults.value = true
    }

    fun saveList(userId: String, name: String) {
        viewModelScope.launch {
            try {
                val ids = _basketNames.value.mapNotNull { bName ->
                    _allProducts.value.find { it.name == bName && it.supermarket == "Mercadona" }?.id
                        ?: _allProducts.value.find { it.name == bName }?.id
                }
                if (ids.isEmpty()) { _error.value = "Basket is empty"; return@launch }
                val r = RetrofitClient.api.createShoppingList(ShoppingListRequest(userId, name, ids))
                if (r.isSuccessful) {
                    _error.value = "✅ List saved!"
                    loadSavedLists(userId)
                } else _error.value = "Save failed: ${r.code()}"
            } catch (e: Exception) { _error.value = "Save failed: ${e.message}" }
        }
    }

    fun loadListIntoBasket(list: com.supercomp.android.data.model.ShoppingList) {
        val resolved = list.resolve(_allProducts.value)
        _basketNames.value = resolved.map { it.name }.distinct()
        _showResults.value = false
        _comparison.value = emptyMap()
    }

    fun renameList(listId: String, newName: String, userId: String) {
        viewModelScope.launch {
            try {
                val existing = _savedLists.value.find { it.id == listId } ?: return@launch
                val ids = existing.resolve(_allProducts.value).map { it.id }
                RetrofitClient.api.deleteShoppingList(listId)
                val r = RetrofitClient.api.createShoppingList(ShoppingListRequest(userId, newName, ids))
                if (r.isSuccessful) { _error.value = "✅ Renamed!"; loadSavedLists(userId) }
            } catch (e: Exception) { _error.value = "Rename failed: ${e.message}" }
        }
    }

    fun loadSavedLists(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val r = RetrofitClient.api.getShoppingListsByUser(userId)
                if (r.isSuccessful) { _savedLists.value = r.body() ?: emptyList(); _error.value = null }
            } catch (_: Exception) { _error.value = "Network error" }
            finally { _isLoading.value = false }
        }
    }

    fun deleteList(id: String, userId: String) {
        viewModelScope.launch {
            try {
                if (RetrofitClient.api.deleteShoppingList(id).isSuccessful) loadSavedLists(userId)
            } catch (_: Exception) {}
        }
    }

    fun toggleFavorite(userId: String, product: Product) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.addToWishlist(WishlistRequest(userId, product.id))
                loadFavoriteIds(userId)
            } catch (_: Exception) {}
        }
    }

    fun clearBasket() {
        _basketNames.value = emptyList()
        _comparison.value  = emptyMap()
        _showResults.value = false
    }

    fun clearError() { _error.value = null }
}

@Composable
fun ShoppingListScreen(
    navController: NavController,
    username: String,
    userId: String,
    viewModel: ShoppingListViewModel = viewModel()
) {
    val basketNames   by viewModel.basketNames.collectAsState()
    val allProducts   by viewModel.allProducts.collectAsState()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val comparison    by viewModel.comparison.collectAsState()
    val showResults   by viewModel.showResults.collectAsState()
    val savedLists    by viewModel.savedLists.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()
    val errorMsg      by viewModel.error.collectAsState()
    val favoriteIds   by viewModel.favoriteIds.collectAsState()

    var selectedTab    by remember { mutableIntStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context           = LocalContext.current
    val voiceParser       = rememberVoiceToTextParser()
    var isListening       by remember { mutableStateOf(false) }

    voiceParser.onResult      = { viewModel.search(it) }
    voiceParser.onStateChange = { isListening = it }

    val micPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) voiceParser.startListening()
    }

    LaunchedEffect(userId) { viewModel.loadSavedLists(userId); viewModel.loadFavoriteIds(userId) }
    LaunchedEffect(errorMsg) { errorMsg?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() } }

    if (showSaveDialog) {
        SaveListDialog(
            onDismiss = { showSaveDialog = false },
            onSave    = { name -> viewModel.saveList(userId, name); showSaveDialog = false; selectedTab = 1 }
        )
    }

    Scaffold(
        bottomBar    = { BottomBar(navController, username, userId) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SuperNavy
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Shopping List Compare 🛒", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SuperTextPrimary)
                Text("Add products and compare totals across supermarkets", color = SuperTextSecond, fontSize = 13.sp)
            }

            TabRow(selectedTabIndex = selectedTab, containerColor = SuperSurface, contentColor = SuperGreen) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Build & Compare") })
                Tab(selected = selectedTab == 1,
                    onClick = { selectedTab = 1; viewModel.loadSavedLists(userId) },
                    text = { Text("Saved Lists") })
            }

            when (selectedTab) {
                0 -> BuildTabContent(
                    query         = searchQuery,
                    searchResults = searchResults,
                    basketNames   = basketNames,
                    allProducts   = allProducts,
                    favoriteIds   = favoriteIds,
                    comparison    = comparison,
                    showResults   = showResults,
                    isListening   = isListening,
                    onSearch      = { viewModel.search(it) },
                    onAdd         = { viewModel.addToBasket(it) },
                    onRemove      = { viewModel.removeFromBasket(it) },
                    onFavorite    = { viewModel.toggleFavorite(userId, it) },
                    onCompare     = { viewModel.performComparison() },
                    onSave        = { showSaveDialog = true },
                    onClear       = { viewModel.clearBasket() },
                    onMap         = { brand -> navController.navigate("map/$brand") },
                    onMicClick    = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {
                            if (isListening) voiceParser.stopListening() else voiceParser.startListening()
                        } else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
                1 -> SavedTabContent(
                    lists       = savedLists,
                    catalog     = allProducts,
                    loading     = isLoading,
                    onDelete    = { viewModel.deleteList(it, userId) },
                    onRename    = { id, name -> viewModel.renameList(id, name, userId) },
                    onEdit      = { list ->
                        viewModel.loadListIntoBasket(list)
                        selectedTab = 0
                    },
                    onMap       = { brand -> navController.navigate("map/$brand") }
                )
            }
        }
    }
}

@Composable
fun BuildTabContent(
    query: String,
    searchResults: List<String>,
    basketNames: List<String>,
    allProducts: List<Product>,
    favoriteIds: Set<String>,
    comparison: Map<String, Double>,
    showResults: Boolean,
    isListening: Boolean,
    onSearch: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onFavorite: (Product) -> Unit,
    onCompare: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onMap: (String) -> Unit,
    onMicClick: () -> Unit
) {
    LazyColumn(
        modifier              = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = query,
                onValueChange = onSearch,
                placeholder   = { Text(if (isListening) "🎤 Listening..." else "Search: leche, pan, agua...", color = SuperTextSecond) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(16.dp),
                singleLine    = true,
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = SuperGreen) },
                trailingIcon  = {
                    if (query.isNotBlank())
                        IconButton(onClick = { onSearch("") }) { Icon(Icons.Default.Close, null, tint = SuperTextSecond) }
                    else
                        IconButton(onClick = onMicClick) {
                            Icon(if (isListening) Icons.Default.Mic else Icons.Default.MicNone, null,
                                tint = if (isListening) Color.Red else SuperGreen)
                        }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = SuperGreen, unfocusedBorderColor = SuperBorder,
                    cursorColor          = SuperGreen, focusedTextColor = SuperTextPrimary, unfocusedTextColor = SuperTextPrimary
                )
            )
        }

        if (searchResults.isNotEmpty()) {
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SuperSurface2),
                    border = BorderStroke(1.dp, SuperBorder)) {
                    Column {
                        searchResults.take(8).forEachIndexed { i, name ->
                            val price = allProducts.filter { it.name == name }.minOfOrNull { it.price } ?: 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onAdd(name) }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(name, color = SuperTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("from €${"%.2f".format(price)}", color = SuperGreen, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.Add, null, tint = SuperGreen, modifier = Modifier.size(20.dp))
                            }
                            if (i < searchResults.size - 1) HorizontalDivider(color = SuperBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        if (basketNames.isNotEmpty()) {
            item {
                Row(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Basket (${basketNames.size})", fontWeight = FontWeight.Bold, color = SuperTextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onClear) { Text("Clear", color = Color.Red.copy(0.7f), fontSize = 12.sp) }
                }
            }

            items(basketNames) { name ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperSurface2),
                    border = BorderStroke(1.dp, SuperBorder)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, Modifier.weight(1f), color = SuperTextPrimary, fontSize = 14.sp)
                        IconButton(onClick = { onRemove(name) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (showResults && comparison.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    ComparisonResultCard(comparison, onMap)
                }

                item {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuperSurface2),
                        border = BorderStroke(1.2.dp, SuperGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, null, tint = SuperGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Shopping List", color = SuperTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (!showResults) {
                item {
                    Button(
                        onClick = onCompare,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuperGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CompareArrows, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Compare Prices", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }

        if (basketNames.isEmpty() && searchResults.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Your basket is empty", color = SuperTextSecond, fontSize = 16.sp)
                        Text("Search and add products above", color = SuperTextSecond, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonResultCard(comparison: Map<String, Double>, onMap: (String) -> Unit) {
    val sorted   = comparison.entries.sortedBy { it.value }
    val minPrice = sorted.firstOrNull()?.value ?: 0.0

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperSurface2),
        border = BorderStroke(1.5.dp, SuperGreen)) {
        Column(Modifier.padding(16.dp)) {
            Text("Price Comparison", fontWeight = FontWeight.Bold, color = SuperTextPrimary, fontSize = 16.sp)
            Text("Total if you buy everything at each supermarket", color = SuperTextSecond, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            sorted.forEachIndexed { index, (market, total) ->
                val isCheapest = index == 0
                val savings    = total - minPrice
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(if (isCheapest) SuperGreen.copy(0.08f) else Color.Transparent, RoundedCornerShape(10.dp))
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(10.dp).background(
                        when (market) { "Mercadona" -> MercadonaGreen; "Lidl" -> LidlBlue; "Carrefour" -> CarrefourBlue; else -> DiaRed },
                        RoundedCornerShape(5.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(market, color = SuperTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    if (isCheapest) {
                        Surface(shape = RoundedCornerShape(6.dp), color = SuperGreen.copy(0.2f)) {
                            Text("CHEAPEST", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp, color = SuperGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { onMap(market) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.LocationOn, null, tint = SuperGreen, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Text("+€${"%.2f".format(savings)}", color = SuperTextSecond, fontSize = 11.sp)
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { onMap(market) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.LocationOn, null, tint = SuperTextSecond, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("€${"%.2f".format(total)}", fontWeight = FontWeight.ExtraBold,
                        color = if (isCheapest) SuperGreen else SuperTextPrimary, fontSize = 16.sp)
                }
                if (index < sorted.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = SuperBorder, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun SaveListDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Shopping List", color = SuperTextPrimary) },
        text  = {
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("List Name") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuperGreen, unfocusedBorderColor = SuperBorder))
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSave(text) },
                colors = ButtonDefaults.buttonColors(containerColor = SuperGreen)) { Text("Save", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = SuperTextSecond) } },
        containerColor = SuperSurface
    )
}

@Composable
fun SavedTabContent(
    lists:    List<com.supercomp.android.data.model.ShoppingList>,
    catalog:  List<Product>,
    loading:  Boolean,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onEdit:   (com.supercomp.android.data.model.ShoppingList) -> Unit,
    onMap:    (String) -> Unit
) {
    if (loading && lists.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = SuperGreen) }
    } else if (lists.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📁", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("No saved lists yet.", color = SuperTextSecond, fontSize = 16.sp)
                Text("Build a list and tap Save.", color = SuperTextSecond, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(lists, key = { it.id }) { list ->
                SavedListCard(
                    list     = list,
                    catalog  = catalog,
                    onDelete = { onDelete(list.id) },
                    onRename = { name -> onRename(list.id, name) },
                    onEdit   = { onEdit(list) },
                    onMap    = onMap
                )
            }
        }
    }
}

@Composable
fun SavedListCard(
    list:     com.supercomp.android.data.model.ShoppingList,
    catalog:  List<Product>,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onEdit:   () -> Unit,
    onMap:    (String) -> Unit
) {
    var expanded       by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val products       = remember(list, catalog) { list.resolve(catalog) }

    val cheapestMarket = remember(products, catalog) {
        if (products.isEmpty()) null
        else {
            val names = products.map { it.name }.distinct()
            val SUPERMARKETS = listOf("Mercadona", "Lidl", "Carrefour", "Dia")
            SUPERMARKETS.mapNotNull { market ->
                var sum = 0.0
                var allFound = true
                for (name in names) {
                    val match = catalog.find { it.name == name && it.supermarket == market }
                    if (match != null) sum += match.price else { allFound = false; break }
                }
                if (allFound) market to sum else null
            }.minByOrNull { it.second }
        }
    }

    if (showEditDialog) {
        RenameListDialog(currentName = list.name,
            onDismiss = { showEditDialog = false },
            onSave    = { newName -> onRename(newName); showEditDialog = false })
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperSurface2),
        border = BorderStroke(1.dp, if (expanded) SuperGreen else SuperBorder)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(list.name, fontWeight = FontWeight.Bold, color = SuperTextPrimary, fontSize = 16.sp)
                        cheapestMarket?.let { (market, price) ->
                            val color = when(market) {
                                "Mercadona" -> MercadonaGreen
                                "Lidl" -> LidlBlue
                                "Carrefour" -> CarrefourBlue
                                else -> DiaRed
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = color.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = color, modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Cheap @ $market",
                                        fontSize = 9.sp,
                                        color = color,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                    Text("${products.size} products · tap to view", fontSize = 12.sp, color = SuperTextSecond)
                }
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = SuperGreen)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = SuperGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DriveFileRenameOutline, null, tint = SuperTextSecond, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.7f), modifier = Modifier.size(18.dp))
                }
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    HorizontalDivider(color = SuperBorder)
                    Spacer(Modifier.height(8.dp))
                    if (products.isEmpty()) {
                        Text("No product details available.", color = SuperTextSecond, fontSize = 12.sp)
                    } else {
                        products.forEach { product ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(product.name, Modifier.weight(1f), color = SuperTextPrimary, fontSize = 13.sp)
                                Text("€${"%.2f".format(product.price)}", color = SuperGreen,
                                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                IconButton(onClick = { onMap(product.supermarket) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.LocationOn, null, tint = SuperTextSecond, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenameListDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename List", color = SuperTextPrimary) },
        text  = {
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("New Name") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuperGreen, unfocusedBorderColor = SuperBorder))
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSave(text) },
                colors = ButtonDefaults.buttonColors(containerColor = SuperGreen)) { Text("Save", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = SuperTextSecond) } },
        containerColor = SuperSurface
    )
}
