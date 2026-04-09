package com.anas.android_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.android_app.data.model.Comment
import com.anas.android_app.data.model.CommentRequest
import com.anas.android_app.data.model.Product
import com.anas.android_app.data.remote.ApiService
import com.anas.android_app.data.remote.ApiServiceInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val api: ApiService = ApiServiceInstance.api
) : ViewModel() {

    // PRODUCTS
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // COMMENTS
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun loadAllProducts() {
        viewModelScope.launch {
            try {
                val response = api.getAllProducts()
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadProductsBySupermarket(supermarket: String) {
        viewModelScope.launch {
            try {
                val response = api.getProductsBySupermarket(supermarket)
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            try {
                val response = api.getAllComments()
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendComment(username: String, message: String) {
        viewModelScope.launch {
            api.sendComment(CommentRequest(username, message))
            loadComments()
        }
    }

}
