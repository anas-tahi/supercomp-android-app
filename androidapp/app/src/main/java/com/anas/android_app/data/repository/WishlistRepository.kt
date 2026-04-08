package com.anas.android_app.data.repository

import android.util.Log
import com.anas.android_app.data.model.Wishlist
import com.anas.android_app.data.model.WishlistRequest
import com.anas.android_app.data.remote.ApiService

class WishlistRepository(private val api: ApiService) {

    private val TAG = "WishlistRepository"

    /** READ with FILTER – get wishlist for a specific user */
    suspend fun getWishlistByUser(userId: String): List<Wishlist> {
        return try {
            val response = api.getWishlistByUser(userId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "getWishlistByUser error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWishlistByUser exception: ${e.message}")
            emptyList()
        }
    }

    /** WRITE – add a product to wishlist */
    suspend fun addToWishlist(userId: String, productId: String): Boolean {
        return try {
            val response = api.addToWishlist(WishlistRequest(userId = userId, productId = productId))
            if (response.isSuccessful) {
                Log.d(TAG, "Added to wishlist: ${response.body()?.message}")
                true
            } else {
                Log.e(TAG, "addToWishlist error: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "addToWishlist exception: ${e.message}")
            false
        }
    }

    /** DELETE – remove a product from wishlist */
    suspend fun removeFromWishlist(wishlistId: String): Boolean {
        return try {
            val response = api.removeFromWishlist(wishlistId)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "removeFromWishlist exception: ${e.message}")
            false
        }
    }
}
