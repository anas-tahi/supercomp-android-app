package com.anas.android_app.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id")
    val id: String = "",

    @SerializedName("name")
    val name: String,

    @SerializedName("supermarket")
    val supermarket: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("__v")
    val v: Int = 0
)
