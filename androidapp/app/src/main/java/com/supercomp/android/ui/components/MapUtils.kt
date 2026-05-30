package com.supercomp.android.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri


fun openMapApp(context: Context, brand: String) {
    val query = when(brand) {
        "Mercadona" -> "Mercadona"
        "Lidl"      -> "Lidl"
        "Carrefour" -> "Carrefour"
        "Dia"       -> "Supermercados Dia"
        else        -> brand
    }
    val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
