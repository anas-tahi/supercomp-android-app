package com.supercomp.android.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.supercomp.android.data.model.Product
import com.supercomp.android.ui.components.BottomBar
import com.supercomp.android.ui.components.ProductImage
import com.supercomp.android.ui.components.supermarketLogoRes
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.delay

data class SupermarketInfo(val name: String, val color: Color, val initial: String)

val supermarketList = listOf(
    SupermarketInfo("Mercadona", MercadonaGreen, "M"),
    SupermarketInfo("Lidl",      LidlBlue,       "L"),
    SupermarketInfo("Carrefour", CarrefourBlue,  "C"),
    SupermarketInfo("Dia",       DiaRed,         "D"),
)

fun supermarketColor(name: String): Color = when (name) {
    "Mercadona" -> MercadonaGreen
    "Lidl"      -> LidlBlue
    "Carrefour" -> CarrefourBlue
    "Dia"       -> DiaRed
    else        -> Color.Gray
}

@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    userId: String,
    viewModel: HomeViewModel = viewModel()
) {
    val products     by viewModel.products.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val favouriteIds by viewModel.favouriteIds.collectAsState()
    val snackMessage by viewModel.snackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val randomProducts = remember(products) {
        if (products.isEmpty()) emptyList() else products.shuffled().take(6)
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllProducts()
        viewModel.loadFavouriteIds(userId)
    }
    LaunchedEffect(snackMessage) {
        snackMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnack() }
    }

    Scaffold(
        bottomBar      = { BottomBar(navController, username, userId) },
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = SuperNavy
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { HeroBanner(username) }
            item { AppInfoStrip() }
            item { AnimatedSupermarketChipsRow() }

            item {
                Row(
                    modifier          = Modifier.padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(4.dp, 20.dp).background(SuperGreen, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Featured Products",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        color      = SuperTextPrimary
                    )
                    Spacer(Modifier.weight(1f))
                    if (isLoading) CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = SuperGreen
                    )
                }
            }

            if (randomProducts.isEmpty() && !isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                        Text("No products yet", color = SuperTextSecond)
                    }
                }
            } else {
                items(randomProducts.chunked(2)) { rowItems ->
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductGridCard(
                                    product     = product,
                                    isFavourite = favouriteIds.contains(product.id),
                                    onFavClick  = { viewModel.toggleFavourite(userId, product, favouriteIds) }
                                )
                            }
                        }
                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(4.dp, 20.dp).background(SuperGreen, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(10.dp))
                        Text("Feedback", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SuperTextPrimary)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value         = commentText,
                        onValueChange = { commentText = it },
                        placeholder   = { Text("¿Algo que mejorar?", color = SuperTextSecond) },
                        shape         = RoundedCornerShape(14.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = SuperGreen,
                            unfocusedBorderColor = SuperBorder,
                            focusedTextColor     = SuperTextPrimary,
                            unfocusedTextColor   = SuperTextPrimary,
                            cursorColor          = SuperGreen
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.sendComment(username, commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = SuperGreen)
                    ) {
                        Icon(Icons.Filled.Send, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enviar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Website promotion
            item {
                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperSurface2),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(SuperGreen, Color.Cyan)))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Beyond the App 🌐",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SuperTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Discover the full SuperComp experience on our official website. More tools, more comparisons, and the same passion for savings.",
                            fontSize = 13.sp,
                            color = SuperTextSecond,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://frontend-12gl.onrender.com"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuperGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Language, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("Visit our Website", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// Hero Banner
@Composable
fun HeroBanner(username: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label         = "offset"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0A1628), Color(0xFF003D1F), Color(0xFF0A1628)),
                    start  = Offset(animatedOffset * 1000f, 0f),
                    end    = Offset(1000f, 500f)
                )
            )
    ) {
        Box(Modifier.size(180.dp).offset((-40).dp, (-40).dp).alpha(0.08f).background(SuperGreen, CircleShape))
        Box(Modifier.size(120.dp).align(Alignment.BottomEnd).offset(30.dp, 30.dp).alpha(0.06f).background(SuperGreen, CircleShape))
        Column(
            modifier            = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("SuperComp 🛒", color = SuperGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text("Hola, $username", color = SuperTextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Find the best grocery prices\nacross Spain's top supermarkets",
                color      = SuperTextSecond,
                fontSize   = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("4", "Supermarkets")
                StatPill("33+", "Products")
                StatPill("Free", "Always")
            }
        }
    }
}

@Composable
fun StatPill(value: String, label: String) {
    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = SuperGreen.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, SuperGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, color = SuperGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text(label, color = SuperTextSecond, fontSize = 11.sp)
        }
    }
}

@Composable
fun AppInfoStrip() {
    Row(
        modifier          = Modifier.fillMaxWidth().background(SuperSurface).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoItem("🔍", "Compare prices")
        VerticalDivider(Modifier.height(32.dp), color = SuperBorder)
        InfoItem("❤️", "Save favourites")
        VerticalDivider(Modifier.height(32.dp), color = SuperBorder)
        InfoItem("🛒", "Compare lists")
    }
}

@Composable
fun RowScope.InfoItem(emoji: String, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.weight(1f)
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(2.dp))
        Text(text, fontSize = 9.sp, color = SuperTextSecond, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

@Composable
fun AnimatedSupermarketChipsRow() {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            "Supermarkets",
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            color      = SuperTextPrimary,
            modifier   = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "chips_scroll")
        val scrollOffset by infiniteTransition.animateFloat(
            initialValue  = 0f,
            targetValue   = 400f,
            animationSpec = infiniteRepeatable(
                animation  = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scroll_offset"
        )

        val listState = rememberLazyListState()

        LaunchedEffect(scrollOffset) {
            try { listState.scrollToItem(0, scrollOffset.toInt().coerceAtLeast(0)) }
            catch (_: Exception) {}
        }

        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled     = true
        ) {
            items(supermarketList + supermarketList) { s ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(supermarketList.indexOf(
                        supermarketList.find { m -> m.name == s.name } ?: supermarketList[0]
                    ) * 100L)
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(400)) + scaleIn(tween(400))
                ) {
                    SupermarketChip(s)
                }
            }
        }
    }
}

@Composable
fun SupermarketChip(info: SupermarketInfo) {
    val logoRes = supermarketLogoRes(info.name)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${info.name}")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.04f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_${info.name}"
    )

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = Color.Transparent,
        border   = BorderStroke(1.5.dp, info.color.copy(alpha = 0.5f)),
        modifier = Modifier.width(108.dp).scale(scale)
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier         = Modifier.size(70.dp).clip(RoundedCornerShape(14.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != null) {
                    Image(
                        painterResource(id = logoRes),
                        info.name,
                        contentScale = ContentScale.Crop,
                        modifier     = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(info.color), contentAlignment = Alignment.Center) {
                        Text(info.initial, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                info.name,
                fontSize   = 11.sp,
                color      = SuperTextPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// Product Grid Card
@Composable
fun ProductGridCard(product: Product, isFavourite: Boolean, onFavClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SuperSurface2),
        border   = BorderStroke(1.dp, SuperBorder)
    ) {
        Column {
            //  `Modifier` use size = 140.dp
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                ProductImage(
                    imageUrl     = product.imageUrl,
                    supermarket  = product.supermarket,
                    size         = 140.dp,
                    cornerRadius = 0.dp,
                    badgeSize    = 24.dp
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onFavClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector        = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint               = if (isFavourite) SuperRed else Color.White,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = SuperTextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.heightIn(min = 36.dp)
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    Text(
                        "€${"%.2f".format(product.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp,
                        color      = SuperGreen
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = supermarketColor(product.supermarket).copy(alpha = 0.2f)
                    ) {
                        Text(
                            product.supermarket.take(1),
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize   = 10.sp,
                            color      = supermarketColor(product.supermarket),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}