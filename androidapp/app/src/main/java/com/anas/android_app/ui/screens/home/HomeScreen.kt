package com.anas.android_app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anas.android_app.ui.components.BottomBar

@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    homeViewModel: HomeViewModel
) {
    val products by homeViewModel.products.collectAsState()
    val comments by homeViewModel.comments.collectAsState()

    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        homeViewModel.loadAllProducts()
        homeViewModel.loadComments()
    }

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Welcome message
            Text(
                text = "Welcome, $username 👋",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            // Supermarket logos
            SupermarketLogosRow()

            Spacer(modifier = Modifier.height(24.dp))

            // Intro text
            Text(
                text = "SuperComp helps you compare products, manage shopping lists, track favorites, and explore the best supermarket options around you.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // FILTER BUTTONS
            Text("Filter by supermarket", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterButton("Lidl") { homeViewModel.loadProductsBySupermarket("Lidl") }
                FilterButton("Dia") { homeViewModel.loadProductsBySupermarket("Dia") }
                FilterButton("Mercadona") { homeViewModel.loadProductsBySupermarket("Mercadona") }
                FilterButton("Carrefour") { homeViewModel.loadProductsBySupermarket("Carrefour") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PRODUCTS LIST
            Text("Products", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (products.isEmpty()) {
                Text("No products found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                products.forEach { product ->
                    Text(
                        text = "${product.name} - ${product.price}€ - ${product.supermarket}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // COMMENT SECTION
            Text("Send us your feedback", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Write your comment here...") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        homeViewModel.sendComment(username, commentText)
                        commentText = ""
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End)
            ) {
                Text("Send")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // COMMENTS LIST
            Text("Comments", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (comments.isEmpty()) {
                Text("No comments yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                comments.forEach { comment ->
                    Text(
                        text = "${comment.username}: ${comment.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SupermarketLogosRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SupermarketLogo("Lidl")
        SupermarketLogo("Dia")
        SupermarketLogo("Mercadona")
        SupermarketLogo("Carrefour")
    }
}

@Composable
fun SupermarketLogo(name: String) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.LightGray, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun FilterButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(label)
    }
}
