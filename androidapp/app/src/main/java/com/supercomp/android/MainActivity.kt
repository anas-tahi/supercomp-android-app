package com.supercomp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.supercomp.android.data.local.UserPrefs
import com.supercomp.android.data.remote.RetrofitClient
import com.supercomp.android.ui.navigation.AppNavGraph
import com.supercomp.android.ui.theme.SuperCompTheme
import com.supercomp.android.ui.theme.SuperGreen
import com.supercomp.android.ui.theme.SuperNavy
import com.supercomp.android.ui.theme.SuperTextSecond
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private fun String.urlEncode(): String =
    URLEncoder.encode(this, StandardCharsets.UTF_8.toString())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Places SDK ← NEW
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyD3YYvzlgr4Ir5MV8JxWe9xdV7p-qZPmzQ")
        }

        setContent {
            SuperCompTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2500)
                    showSplash = false
                }

                if (showSplash) SplashScreen()
                else MainContent()
            }
        }
    }
}

@Composable
fun SplashScreen() {
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoVisible = true
        delay(400)
        textVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SuperNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val scale by animateFloatAsState(
                targetValue = if (logoVisible) 1f else 0.5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "logoScale"
            )

            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_supercomp_logo),
                    contentDescription = "SuperComp Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(32.dp))
                )
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SuperComp",
                        color = SuperGreen,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Comparador de Precios · España",
                        color = SuperTextSecond,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = SuperGreen,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val context = LocalContext.current
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            val prefs = UserPrefs(context)
            val token = prefs.getToken()
            val username = prefs.getUsername()
            val userId = prefs.getUserId()

            if (!token.isNullOrBlank() && !username.isNullOrBlank() && !userId.isNullOrBlank()) {
                RetrofitClient.authToken = token
                navController.navigate(
                    "home/${username.urlEncode()}/${userId.urlEncode()}"
                ) {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        AppNavGraph(navController = navController)
    }
}