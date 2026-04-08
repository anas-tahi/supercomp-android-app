package com.anas.android_app.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anas.android_app.ui.components.BottomBar

@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    onSendComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

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

            Spacer(modifier = Modifier.height(32.dp))

            // Comment section
            Text(
                text = "Send us your feedback",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Write your comment here...") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendComment(commentText)
                        commentText = ""
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End)
            ) {
                Text("Send")
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
