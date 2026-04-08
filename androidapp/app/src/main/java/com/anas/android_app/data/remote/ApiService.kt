package com.anas.android_app.data.remote

import com.anas.android_app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── AUTH ───────────────────────────────────────────────
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BasicResponse>

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ─── PRODUCTS ────────────────────────────────────────────
    /** Get all products */
    @GET("/products")
    suspend fun getAllProducts(): Response<List<Product>>

    /** Get products filtered by supermarket */
    @GET("/products/supermarket/{name}")
    suspend fun getProductsBySupermarket(
        @Path("name") supermarket: String
    ): Response<List<Product>>

    /** Get products filtered by name (for comparison) */
    @GET("/products/search")
    suspend fun searchProducts(
        @Query("name") name: String
    ): Response<List<Product>>

    // ─── COMMENTS ────────────────────────────────────────────
    /** Get all comments */
    @GET("/comments")
    suspend fun getAllComments(): Response<List<Comment>>

    /** Post a new comment */
    @POST("/comments")
    suspend fun postComment(@Body request: CommentRequest): Response<BasicResponse>

    // ─── SHOPPING LISTS ──────────────────────────────────────
    /** Get all shopping lists for a user */
    @GET("/shoppinglists/user/{userId}")
    suspend fun getShoppingListsByUser(
        @Path("userId") userId: String
    ): Response<List<ShoppingList>>

    /** Create a new shopping list */
    @POST("/shoppinglists")
    suspend fun createShoppingList(@Body request: ShoppingListRequest): Response<BasicResponse>

    /** Delete a shopping list */
    @DELETE("/shoppinglists/{id}")
    suspend fun deleteShoppingList(@Path("id") id: String): Response<BasicResponse>

    // ─── WISHLIST ────────────────────────────────────────────
    /** Get wishlist for a user */
    @GET("/wishlist/user/{userId}")
    suspend fun getWishlistByUser(
        @Path("userId") userId: String
    ): Response<List<Wishlist>>

    /** Add product to wishlist */
    @POST("/wishlist")
    suspend fun addToWishlist(@Body request: WishlistRequest): Response<BasicResponse>

    /** Remove product from wishlist */
    @DELETE("/wishlist/{id}")
    suspend fun removeFromWishlist(@Path("id") id: String): Response<BasicResponse>
}
