package com.anas.android_app.data.remote

import com.anas.android_app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // AUTH
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BasicResponse>

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // PRODUCTS
    @GET("/products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("/products/supermarket/{name}")
    suspend fun getProductsBySupermarket(
        @Path("name") supermarket: String
    ): Response<List<Product>>

    @GET("/products/search")
    suspend fun searchProducts(
        @Query("name") name: String
    ): Response<List<Product>>

    // COMMENTS
    @GET("/comments")
    suspend fun getAllComments(): Response<List<Comment>>

    @POST("/comments")
    suspend fun sendComment(@Body request: CommentRequest): Response<BasicResponse>

    // SHOPPING LISTS
    @GET("/shoppinglists/user/{userId}")
    suspend fun getShoppingListsByUser(
        @Path("userId") userId: String
    ): Response<List<ShoppingList>>

    @POST("/shoppinglists")
    suspend fun createShoppingList(@Body request: ShoppingListRequest): Response<BasicResponse>

    @DELETE("/shoppinglists/{id}")
    suspend fun deleteShoppingList(@Path("id") id: String): Response<BasicResponse>

    // WISHLIST
    @GET("/wishlist/user/{userId}")
    suspend fun getWishlistByUser(
        @Path("userId") userId: String
    ): Response<List<Wishlist>>

    @POST("/wishlist")
    suspend fun addToWishlist(@Body request: WishlistRequest): Response<BasicResponse>

    @DELETE("/wishlist/{id}")
    suspend fun removeFromWishlist(@Path("id") id: String): Response<BasicResponse>
}
