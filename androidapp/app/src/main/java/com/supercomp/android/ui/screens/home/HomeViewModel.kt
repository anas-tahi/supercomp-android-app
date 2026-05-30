package com.supercomp.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supercomp.android.data.model.*
import com.supercomp.android.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _favouriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favouriteIds: StateFlow<Set<String>> = _favouriteIds

    // Map: productId -> entryId (the record ID in the wishlist table)
    private val wishlistMap = mutableMapOf<String, String>()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage

    fun clearSnack() { _snackMessage.value = null }

    fun loadAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val r = RetrofitClient.api.getAllProducts()
                if (r.isSuccessful) _products.value = r.body() ?: emptyList()
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    private suspend fun loadFavouriteIdsSuspend(userId: String) {
        try {
            val r = RetrofitClient.api.getWishlistByUser(userId)
            if (r.isSuccessful) {
                val items = r.body() ?: emptyList()
                val ids = mutableSetOf<String>()
                wishlistMap.clear()
                items.forEach { w ->
                    val pid = w.getSafeProductId()
                    if (pid.isNotBlank()) {
                        ids.add(pid)
                        wishlistMap[pid] = w.id
                    }
                }
                _favouriteIds.value = ids
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadFavouriteIds(userId: String) {
        viewModelScope.launch { loadFavouriteIdsSuspend(userId) }
    }

    fun toggleFavourite(userId: String, product: Product, currentFavs: Set<String>) {
        viewModelScope.launch {
            try {
                if (currentFavs.contains(product.id)) {
                    val entryId = wishlistMap[product.id] ?: return@launch
                    val r = RetrofitClient.api.removeFromWishlist(entryId)
                    if (r.isSuccessful) {
                        _favouriteIds.value = currentFavs - product.id
                        wishlistMap.remove(product.id)
                        _snackMessage.value = "Removed from favourites"
                    }
                } else {
                    val r = RetrofitClient.api.addToWishlist(WishlistRequest(userId, product.id))
                    if (r.isSuccessful) {
                        // Turn it red immediately (Optimistic UI)
                        _favouriteIds.value = currentFavs + product.id
                        // Sync with server to get the database ID for future removal
                        loadFavouriteIdsSuspend(userId)
                        _snackMessage.value = "Added to favourites ❤️"
                    }
                }
            } catch (e: Exception) { _snackMessage.value = "Error: ${e.message}" }
        }
    }

    fun sendComment(username: String, message: String) {
        viewModelScope.launch {
            try { RetrofitClient.api.sendComment(CommentRequest(username, message)) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }
}
