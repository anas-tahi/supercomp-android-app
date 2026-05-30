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
                contentScale       = ContentScale.Crop, // Full fill
                modifier           = Modifier.fillMaxSize()
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
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(cornerRadius))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoRes != null) {
                            Image(painterResource(logoRes), supermarket,
                                contentScale = ContentScale.Crop, // Full fill the box
                                modifier = Modifier.fillMaxSize())
                        } else {
                            Text(supermarket.take(1), color = color,
                                fontSize = fallbackFontSize, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            )
        } else {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(cornerRadius)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != null) {
                    Image(painterResource(logoRes), supermarket,
                        contentScale = ContentScale.Crop, // Full fill the box
                        modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(supermarket.take(1), color = color,
                            fontSize = fallbackFontSize, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

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
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        }
    }
}
