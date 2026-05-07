package com.supercomp.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.supercomp.android.data.model.*
import com.supercomp.android.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _activeFilter = MutableStateFlow("All")
    val activeFilter: StateFlow<String> = _activeFilter

    // Set of product IDs already in the wishlist
    private val _favouriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favouriteIds: StateFlow<Set<String>> = _favouriteIds

    // Map wishlist entry ID → product ID (for deletion)
    private val wishlistMap = mutableMapOf<String, String>() // productId -> wishlistEntryId

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage

    fun clearSnack() { _snackMessage.value = null }

    fun loadAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _activeFilter.value = "All"
            try {
                val r = RetrofitClient.api.getAllProducts()
                if (r.isSuccessful) _products.value = r.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun loadProductsBySupermarket(supermarket: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _activeFilter.value = supermarket
            try {
                val r = RetrofitClient.api.getProductsBySupermarket(supermarket)
                if (r.isSuccessful) _products.value = r.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getAllComments()
                if (r.isSuccessful) _comments.value = r.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadFavouriteIds(userId: String) {
        viewModelScope.launch {
            try {
                val r = RetrofitClient.api.getWishlistByUser(userId)
                if (r.isSuccessful) {
                    val items = r.body() ?: emptyList()
                    wishlistMap.clear()
                    val ids = mutableSetOf<String>()
                    items.forEach { w ->
                        val productId = extractProductId(w.productId)
                        if (productId.isNotBlank()) {
                            ids.add(productId)
                            wishlistMap[productId] = w.id
                        }
                    }
                    _favouriteIds.value = ids
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun toggleFavourite(userId: String, product: Product, currentFavs: Set<String>) {
        viewModelScope.launch {
            try {
                if (currentFavs.contains(product.id)) {
                    // Remove
                    val entryId = wishlistMap[product.id] ?: return@launch
                    RetrofitClient.api.removeFromWishlist(entryId)
                    _favouriteIds.value = currentFavs - product.id
                    wishlistMap.remove(product.id)
                    _snackMessage.value = "Removed from favourites"
                } else {
                    // Add
                    val r = RetrofitClient.api.addToWishlist(WishlistRequest(userId, product.id))
                    if (r.isSuccessful) {
                        _favouriteIds.value = currentFavs + product.id
                        // Reload to get the entry ID for future removal
                        loadFavouriteIds(userId)
                        _snackMessage.value = "Added to favourites ❤️"
                    } else {
                        _snackMessage.value = "Already in favourites"
                    }
                }
            } catch (e: Exception) { _snackMessage.value = "Error: ${e.message}" }
        }
    }

    fun sendComment(username: String, message: String) {
        viewModelScope.launch {
            try { RetrofitClient.api.sendComment(CommentRequest(username, message)); loadComments() }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun extractProductId(productId: Any): String {
        return try {
            when (productId) {
                is String -> productId
                else -> {
                    val json = Gson().toJson(productId)
                    val p = Gson().fromJson(json, Product::class.java)
                    p.id
                }
            }
        } catch (e: Exception) { "" }
    }
}
