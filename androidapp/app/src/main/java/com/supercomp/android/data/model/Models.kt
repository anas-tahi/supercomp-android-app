package com.supercomp.android.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

fun Any?.toIdString(): String {
    if (this == null) return ""
    if (this is String) return this
    if (this is Map<*, *>) {
        return this["\$oid"]?.toString() 
            ?: this["_id"]?.toString() 
            ?: this["id"]?.toString() 
            ?: ""
    }
    return this.toString()
}

data class Product(
    @SerializedName("_id") private val _id1: Any? = null,
    @SerializedName("id")  private val _id2: Any? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("supermarket") val supermarket: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("category") val category: String = "General",
    @SerializedName("imageUrl") val imageUrl: String = ""
) {
    val id: String get() = _id1.toIdString().ifBlank { _id2.toIdString() }
}

data class ShoppingList(
    @SerializedName("_id")       val idRaw: Any?          = null,
    @SerializedName("user")      val userId: String       = "",
    @SerializedName("name")      val name: String         = "",
    @SerializedName("products")  val products: List<Any>? = null,
    @SerializedName("items")     val items: List<Any>?    = null,
    @SerializedName("createdAt") val createdAt: String    = ""
) {
    val id: String get() = idRaw.toIdString()


    fun resolve(catalog: List<Product>): List<Product> {
        val raw = products ?: items ?: return emptyList()
        return raw.mapNotNull { item ->
            if (item is Map<*, *>) {
                try {
                    val json = Gson().toJson(item)
                    val p = Gson().fromJson(json, Product::class.java).takeIf { it.id.isNotBlank() }
                    p
                } catch(e: Exception) { null }
            } else {
                val pid = item.toIdString()
                catalog.find { it.id == pid }
            }
        }
    }
    
    val itemCount: Int get() = (products ?: items ?: emptyList()).size
}

data class Wishlist(
    @SerializedName("_id")       val idRaw: Any? = null,
    @SerializedName("userId")    val userId: String = "",
    @SerializedName("productId") val productId: Any? = null
) {
    val id: String get() = idRaw.toIdString()
    fun getSafeProductId(): String = productId.toIdString()

    fun resolveProduct(catalog: List<Product>): Product? {
        if (productId is Map<*, *>) {
            try {
                val json = Gson().toJson(productId)
                val p = Gson().fromJson(json, Product::class.java)
                if (p.id.isNotBlank()) return p
            } catch (e: Exception) {}
        }
        val pid = getSafeProductId()
        return catalog.find { it.id == pid }
    }
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val message: String, 
    val token: String, 
    val username: String, 
    val userId: String, 
    val email: String = "",
    val profilePicture: String = "",
    val phone: String = "",
    val city: String = ""
)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class BasicResponse(val message: String? = null, val error: String? = null, val username: String? = null, val profilePicture: String? = null, val phone: String? = null, val city: String? = null)
data class Comment(@SerializedName("_id") val id: String = "", val username: String = "Anonymous", val message: String = "")
data class CommentRequest(val username: String, val message: String)
data class ShoppingListRequest(val user: String, val name: String, val products: List<String>)
data class WishlistRequest(val userId: String, val productId: String)
data class UserProfile(
    @SerializedName("_id") private val _id: Any? = null, 
    val username: String = "", 
    val email: String = "", 
    val phone: String = "", 
    val city: String = "",
    val profilePicture: String = ""
) {
    val id: String get() = _id.toIdString()
}
data class UpdateProfileRequest(
    val username: String? = null, 
    val phone: String? = null, 
    val city: String? = null, 
    val currentPassword: String? = null, 
    val newPassword: String? = null,
    val profilePicture: String? = null
)
