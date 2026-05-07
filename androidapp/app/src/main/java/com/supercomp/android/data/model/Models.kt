package com.supercomp.android.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id")         val id: String          = "",
    @SerializedName("name")        val name: String        = "",
    @SerializedName("supermarket") val supermarket: String = "",
    @SerializedName("price")       val price: Double       = 0.0,
    @SerializedName("category")    val category: String    = "General",
    @SerializedName("imageUrl")    val imageUrl: String    = ""
)

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("message")  val message: String,
    @SerializedName("token")    val token: String,
    @SerializedName("username") val username: String,
    @SerializedName("userId")   val userId: String,
    @SerializedName("email")    val email: String = ""
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class BasicResponse(
    @SerializedName("message")  val message: String?  = null,
    @SerializedName("error")    val error: String?    = null,
    @SerializedName("username") val username: String? = null
)

// FIX 4: username and message are non-nullable with safe defaults
data class Comment(
    @SerializedName("_id")       val id: String        = "",
    @SerializedName("username")  val username: String  = "Anonymous",
    @SerializedName("message")   val message: String   = "",
    @SerializedName("createdAt") val createdAt: String = ""
)

// FIX 5: CommentRequest uses "username" field (matches backend Comment model)
data class CommentRequest(
    @SerializedName("username") val username: String,
    @SerializedName("message")  val message: String
)

data class ShoppingList(
    @SerializedName("_id")       val id: String          = "",
    @SerializedName("user")      val userId: String      = "",
    @SerializedName("name")      val name: String        = "",
    @SerializedName("products")  val products: List<Product> = emptyList(),
    @SerializedName("createdAt") val createdAt: String   = ""
)

data class ShoppingListRequest(
    @SerializedName("user")     val userId: String,
    @SerializedName("name")     val name: String,
    @SerializedName("products") val products: List<String>  // product IDs
)

data class Wishlist(
    @SerializedName("_id")       val id: String = "",
    @SerializedName("userId")    val userId: String = "",
    @SerializedName("productId") val productId: Any = ""  // can be String or populated Object
)

data class WishlistRequest(
    @SerializedName("userId")    val userId: String,
    @SerializedName("productId") val productId: String
)

data class UserProfile(
    @SerializedName("_id")      val id: String       = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email")    val email: String    = ""
)

data class UpdateProfileRequest(
    @SerializedName("username")        val username: String?        = null,
    @SerializedName("currentPassword") val currentPassword: String? = null,
    @SerializedName("newPassword")     val newPassword: String?     = null
)