package com.anas.android_app.data.model

import com.google.gson.annotations.SerializedName

data class Wishlist(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("userId")
    val userId: String,

    @SerializedName("productId")
    val productId: String
)

data class WishlistRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("productId")
    val productId: String
)
