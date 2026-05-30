package com.supercomp.android.ui.screens.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supercomp.android.data.model.Product
import com.supercomp.android.ui.theme.*

data class SupermarketTotal(
    val name: String,
    val total: Double,
    val color: Color,
    val isCheapest: Boolean = false
)

@Composable
fun SupermarketComparisonCard(
    basket: List<Product>,
    allProducts: List<Product>,
    modifier: Modifier = Modifier
) {
    if (basket.isEmpty()) return

    // Calculate totals per supermarket using all products prices
    val supermarketTotals = calculateSupermarketTotals(basket, allProducts)
    
    if (supermarketTotals.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SuperSurface2),
            border = BorderStroke(1.dp, SuperBorder)
        ) {
            Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Not enough data to compare all items", color = SuperTextSecond, fontSize = 14.sp)
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SuperSurface2),
        border = BorderStroke(1.dp, SuperBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Price Comparison",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SuperGreen
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                "Total prices for your list",
                style = MaterialTheme.typography.bodyMedium,
                color = SuperTextSecond
            )
            
            Spacer(Modifier.height(16.dp))

            supermarketTotals.forEach { total ->
                SupermarketTotalRow(total)
                Spacer(Modifier.height(8.dp))
            }

            val cheapest = supermarketTotals.minByOrNull { it.total }
            if (cheapest != null) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SuperGreen.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, SuperGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuperGreen
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Best Deal Found!",
                                fontWeight = FontWeight.Bold,
                                color = SuperGreen,
                                fontSize = 14.sp
                            )
                            Text(
                                "Compre en ${cheapest.name} por solo €${"%.2f".format(cheapest.total)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuperTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupermarketTotalRow(total: SupermarketTotal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (total.isCheapest) SuperGreen.copy(alpha = 0.05f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = if (total.isCheapest) BorderStroke(1.dp, SuperGreen) else BorderStroke(1.dp, SuperBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(total.color, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    total.name,
                    color = SuperTextPrimary,
                    fontWeight = if (total.isCheapest) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "€${"%.2f".format(total.total)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (total.isCheapest) SuperGreen else SuperTextPrimary
                )
                
                if (total.isCheapest) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, null, tint = SuperGreen, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

fun calculateSupermarketTotals(basket: List<Product>, allProducts: List<Product>): List<SupermarketTotal> {
    if (basket.isEmpty()) return emptyList()
    
    val productNames = basket.map { it.name.lowercase().trim() }.distinct()
    
    val supermarkets = listOf(
        "Mercadona" to MercadonaGreen,
        "Lidl" to LidlBlue,
        "Carrefour" to CarrefourBlue,
        "Dia" to DiaRed
    )
    
    val totals = supermarkets.mapNotNull { (name, color) ->
        var total = 0.0
        var foundAllProducts = true
        
        productNames.forEach { productName ->
            val productInSupermarket = allProducts.firstOrNull { 
                it.name.lowercase().trim() == productName && it.supermarket == name 
            }
            
            if (productInSupermarket != null) {
                total += productInSupermarket.price
            } else {
                foundAllProducts = false
            }
        }
        
        if (foundAllProducts && total > 0) {
            SupermarketTotal(name, total, color)
        } else {
            null
        }
    }
    
    if (totals.isEmpty()) return emptyList()
    
    val minTotal = totals.minByOrNull { it.total }?.total ?: 0.0
    return totals.map { it.copy(isCheapest = Math.abs(it.total - minTotal) < 0.001) }
}
