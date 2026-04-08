package com.anas.android_app.data.repository

import android.util.Log
import com.anas.android_app.data.model.ShoppingList
import com.anas.android_app.data.model.ShoppingListRequest
import com.anas.android_app.data.remote.ApiService

class ShoppingListRepository(private val api: ApiService) {

    private val TAG = "ShoppingListRepository"

    /** READ with FILTER – get all lists for a specific user */
    suspend fun getListsByUser(userId: String): List<ShoppingList> {
        return try {
            val response = api.getShoppingListsByUser(userId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "getListsByUser error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getListsByUser exception: ${e.message}")
            emptyList()
        }
    }

    /** WRITE – save a new shopping list */
    suspend fun createList(userId: String, name: String, items: List<String>): Boolean {
        return try {
            val request = ShoppingListRequest(userId = userId, name = name, items = items)
            val response = api.createShoppingList(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Shopping list created: ${response.body()?.message}")
                true
            } else {
                Log.e(TAG, "createList error: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "createList exception: ${e.message}")
            false
        }
    }

    /** DELETE – remove a shopping list by id */
    suspend fun deleteList(id: String): Boolean {
        return try {
            val response = api.deleteShoppingList(id)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "deleteList exception: ${e.message}")
            false
        }
    }
}
