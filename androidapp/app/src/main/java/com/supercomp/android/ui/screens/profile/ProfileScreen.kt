package com.supercomp.android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.supercomp.android.data.local.UserPrefs
import com.supercomp.android.data.model.UpdateProfileRequest
import com.supercomp.android.data.remote.RetrofitClient
import com.supercomp.android.ui.components.BottomBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearMessages() { _successMessage.value = null; _errorMessage.value = null }

    fun updateProfile(
        userId: String,
        newUsername: String?,
        currentPassword: String?,
        newPassword: String?,
        onUsernameChanged: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateProfileRequest(
                    username = newUsername?.takeIf { it.isNotBlank() },
                    currentPassword = currentPassword?.takeIf { it.isNotBlank() },
                    newPassword = newPassword?.takeIf { it.isNotBlank() }
                )
                val r = RetrofitClient.api.updateProfile(userId, request)
                if (r.isSuccessful) {
                    val updatedUsername = r.body()?.username
                    if (updatedUsername != null) onUsernameChanged(updatedUsername)
                    _successMessage.value = "Profile updated successfully!"
                } else {
                    _errorMessage.value = "Update failed. Check your current password."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    username: String,
    userId: String,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var currentUsername by remember { mutableStateOf(username) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            UserPrefs(context).clearSession()
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log out") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentUsername = currentUsername,
            isLoading = isLoading,
            onDismiss = { showEditDialog = false },
            onSave = { newUsername, currentPwd, newPwd ->
                viewModel.updateProfile(
                    userId = userId,
                    newUsername = newUsername,
                    currentPassword = currentPwd,
                    newPassword = newPwd,
                    onUsernameChanged = { updated ->
                        currentUsername = updated
                        scope.launch { UserPrefs(context).updateUsername(updated) }
                        showEditDialog = false
                    }
                )
            }
        )
    }

    Scaffold(
        bottomBar = { BottomBar(navController, currentUsername, userId) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(currentUsername.take(1).uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(currentUsername, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("SuperComp member", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            // Edit button
            OutlinedButton(
                onClick = { showEditDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileInfoRow("Username", currentUsername)
                    HorizontalDivider()
                    ProfileInfoRow("User ID", userId.take(8) + "...")
                    HorizontalDivider()
                    ProfileInfoRow("App version", "v3.0")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log out", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUsername: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (newUsername: String, currentPwd: String, newPwd: String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }
    var currentPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var pwdError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Text("Username", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = newUsername, onValueChange = { newUsername = it },
                    label = { Text("New username") }, singleLine = true,
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Text("Change Password (optional)", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = currentPwd, onValueChange = { currentPwd = it },
                    label = { Text("Current password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPwd, onValueChange = { newPwd = it; pwdError = "" },
                    label = { Text("New password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPwd, onValueChange = { confirmPwd = it; pwdError = "" },
                    label = { Text("Confirm new password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )

                if (pwdError.isNotBlank()) {
                    Text(pwdError, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPwd.isNotBlank() && newPwd != confirmPwd) {
                        pwdError = "Passwords do not match."
                        return@Button
                    }
                    onSave(newUsername, currentPwd, newPwd)
                },
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
