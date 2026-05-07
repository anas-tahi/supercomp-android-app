package com.supercomp.android.ui.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.supercomp.android.R
import com.supercomp.android.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (username: String, userId: String) -> Unit,
    onGoToRegister: () -> Unit
) {
    val context = LocalContext.current
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(SuperNavy)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Logo ─────────────────────────────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.ic_supercomp_logo),
            contentDescription = "SuperComp",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "SuperComp",
            color      = SuperGreen,
            fontSize   = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            "Comparador de Precios · España",
            color     = SuperTextSecond,
            fontSize  = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        // ── Form card ────────────────────────────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(20.dp),
            color  = SuperSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Iniciar sesión",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = SuperTextPrimary
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value               = password,
                    onValueChange       = { password = it },
                    label               = { Text("Contraseña") },
                    singleLine          = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape               = RoundedCornerShape(12.dp),
                    modifier            = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Por favor rellena todos los campos."
                            return@Button
                        }
                        isLoading = true; errorMessage = ""
                        viewModel.login(context, email, password,
                            onSuccess = { username, userId ->
                                isLoading = false
                                onLoginSuccess(username, userId)
                            },
                            onError = { msg -> isLoading = false; errorMessage = msg }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = !isLoading,
                    colors   = ButtonDefaults.buttonColors(containerColor = SuperGreen)
                ) {
                    if (isLoading) CircularProgressIndicator(color = SuperNavy, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Entrar", fontWeight = FontWeight.Bold, color = SuperNavy)
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(errorMessage, color = SuperRed, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        TextButton(onClick = onGoToRegister) {
            Text("¿No tienes cuenta? ", color = SuperTextSecond)
            Text("Regístrate", color = SuperGreen, fontWeight = FontWeight.Bold)
        }
    }
}
