package com.anas.android_app.data.model

import com.google.gson.annotations.SerializedName

data class ShoppingList(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("user")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("items")
    val items: List<String>,

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = ""
)

data class ShoppingListRequest(
    @SerializedName("user")
    val userId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("items")
    val items: List<String>
)
