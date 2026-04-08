package com.anas.android_app.data.repository

import android.util.Log
import com.anas.android_app.data.model.Product
import com.anas.android_app.data.remote.ApiService

class ProductRepository(private val api: ApiService) {

    private val TAG = "ProductRepository"

    /** READ – all products */
    suspend fun getAllProducts(): List<Product> {
        return try {
            val response = api.getAllProducts()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "getAllProducts error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllProducts exception: ${e.message}")
            emptyList()
        }
    }

    /** READ with FILTER – products by supermarket (e.g. "Mercadona") */
    suspend fun getProductsBySupermarket(supermarket: String): List<Product> {
        return try {
            val response = api.getProductsBySupermarket(supermarket)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "getProductsBySupermarket error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProductsBySupermarket exception: ${e.message}")
            emptyList()
        }
    }

    /** READ with FILTER – search products by name for comparison */
    suspend fun searchProducts(name: String): List<Product> {
        return try {
            val response = api.searchProducts(name)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "searchProducts error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchProducts exception: ${e.message}")
            emptyList()
        }
    }
}
