package com.anas.android_app.ui.screens.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.android_app.data.model.Product
import com.anas.android_app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompareUiState(
    val products: List<Product> = emptyList(),
    val loading: Boolean = false,
    val status: String? = null
)

class CompareViewModel : ViewModel() {

    private val repo = ProductRepository()

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            val products = repo.getAllProducts()
            _uiState.update {
                it.copy(
                    loading = false,
                    products = products,
                    status = "Showing all ${products.size} products"
                )
            }
        }
    }

    fun filterBy(supermarket: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            val products = repo.getProductsBySupermarket(supermarket)
            _uiState.update {
                it.copy(
                    loading = false,
                    products = products,
                    status = "${products.size} products from $supermarket"
                )
            }
        }
    }
}