package com.anas.android_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anas.android_app.ui.screens.home.HomeScreen

@Composable
fun AppNavHost(
    username: String,
    onSendComment: (String) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                navController = navController,
                username = username,
                onSendComment = onSendComment
            )
        }

        composable("product_compare") { EmptyScreen("Product Compare") }
        composable("list_compare") { EmptyScreen("List Compare") }
        composable("favorites") { EmptyScreen("Favorites") }
        composable("profile") { EmptyScreen("Profile") }
    }
}

@Composable
fun EmptyScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title Screen")
    }
}
