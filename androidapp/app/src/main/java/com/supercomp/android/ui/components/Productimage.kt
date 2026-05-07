package com.supercomp.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.supercomp.android.R
import com.supercomp.android.ui.screens.home.supermarketColor

fun supermarketLogoRes(supermarket: String): Int? = when (supermarket) {
    "Mercadona" -> R.drawable.supermarket_mercadona
    "Lidl"      -> R.drawable.supermarket_lidl
    "Carrefour" -> R.drawable.supermarket_carrefour
    "Dia"       -> R.drawable.supermarket_dia
    else        -> null
}

/**
 * Standalone supermarket logo chip — used in the home chips row and as a fallback.
 */
@Composable
fun SupermarketLogo(
    supermarket: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 12.dp,
    fallbackFontSize: TextUnit = 20.sp
) {
    val color   = supermarketColor(supermarket)
    val logoRes = supermarketLogoRes(supermarket)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (logoRes != null) {
            Image(
                painter            = painterResource(id = logoRes),
                contentDescription = supermarket,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(supermarket.take(1), color = color,
                    fontSize = fallbackFontSize, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/**
 * Product card image: shows the product photo from imageUrl.
 * In the bottom-right corner a small supermarket logo badge is overlaid.
 */
@Composable
fun ProductImage(
    imageUrl: String,
    supermarket: String,
    size: Dp = 64.dp,
    cornerRadius: Dp = 12.dp,
    badgeSize: Dp = 22.dp,
    fallbackFontSize: TextUnit = 22.sp
) {
    val color   = supermarketColor(supermarket)
    val logoRes = supermarketLogoRes(supermarket)

    Box(modifier = Modifier.size(size)) {
        // ── Main product photo ────────────────────────────────────────────────
        if (imageUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model              = imageUrl,
                contentDescription = supermarket,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                loading = {
                    Box(
                        Modifier.fillMaxSize().background(color.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(supermarket.take(1), color = color,
                            fontSize = fallbackFontSize, fontWeight = FontWeight.ExtraBold)
                    }
                },
                error = {
                    // If photo fails, fall back to logo
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(cornerRadius))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoRes != null) {
                            Image(painterResource(logoRes), supermarket,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(6.dp))
                        } else {
                            Text(supermarket.take(1), color = color,
                                fontSize = fallbackFontSize, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            )
        } else {
            // No URL — show supermarket logo full-size
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(cornerRadius)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != null) {
                    Image(painterResource(logoRes), supermarket,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(6.dp))
                } else {
                    Box(Modifier.fillMaxSize().background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(supermarket.take(1), color = color,
                            fontSize = fallbackFontSize, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // ── Supermarket logo badge (bottom-right corner) ──────────────────────
        if (imageUrl.isNotBlank() && logoRes != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(badgeSize)
                    .clip(RoundedCornerShape(topStart = cornerRadius / 2))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(logoRes),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        }
    }
}
