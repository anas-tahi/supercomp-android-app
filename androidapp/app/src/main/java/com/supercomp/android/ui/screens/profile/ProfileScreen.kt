package com.supercomp.android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.supercomp.android.data.local.UserPrefs
import com.supercomp.android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    username: String,
    userId: String,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    val scope = rememberCoroutineScope()
    val viewModel: ProfileViewModel = viewModel()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentUserId by remember { mutableStateOf<String?>(userId) }
    var currentUsername by remember { mutableStateOf(username) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("") }

    var newUsername by remember { mutableStateOf(username) }
    var newPhone by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPasswordFields by remember { mutableStateOf(false) }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { profilePicture = it.toString() }
    }

    LaunchedEffect(Unit) {
        email = prefs.getEmail() ?: ""
        phone = prefs.getPhone() ?: ""
        city = prefs.getCity() ?: ""
        profilePicture = prefs.getProfilePicture() ?: ""
        newPhone = phone
        newCity = city
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SuperSurface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "SuperComp Menu",
                    modifier = Modifier.padding(20.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuperGreen
                )
                HorizontalDivider(color = SuperBorder)
                
                val items = listOf(
                    "Home" to Icons.Filled.Home,
                    "Compare" to Icons.Filled.Search,
                    "Saved" to Icons.Filled.Favorite,
                    "Lists" to Icons.AutoMirrored.Filled.List,
                    "Profile" to Icons.Filled.Person
                )
                
                Column(modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)) {
                    for (item in items) {
                        val (label, icon) = item
                        val route = when(label) {
                            "Home" -> "home/$username/$userId"
                            "Compare" -> "compare/$username/$userId"
                            "Saved" -> "favorites/$username/$userId"
                            "Lists" -> "shoppinglist/$username/$userId"
                            else -> "profile/$username/$userId"
                        }
                        
                        NavigationDrawerItem(
                            label = { Text(label, color = SuperTextPrimary) },
                            selected = label == "Profile",
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (label != "Profile") {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(icon, null, tint = if (label == "Profile") SuperGreen else SuperTextSecond) },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = SuperGreen.copy(alpha = 0.1f),
                                unselectedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mi Perfil", color = SuperTextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = SuperGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SuperSurface
                    )
                )
            },
            containerColor = SuperNavy
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SuperSurface2)
                        .border(3.dp, SuperGreen, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePicture.isNotBlank()) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(60.dp),
                            tint = SuperTextSecond
                        )
                    }
                }

                TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = SuperGreen)
                    Spacer(Modifier.width(4.dp))
                    Text("Cambiar foto", color = SuperGreen)
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Nombre de usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SuperGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SuperGreen,
                        unfocusedBorderColor = SuperBorder,
                        focusedTextColor = SuperTextPrimary,
                        unfocusedTextColor = SuperTextPrimary
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SuperGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = SuperTextSecond,
                        disabledBorderColor = SuperBorder.copy(alpha = 0.5f),
                        disabledLabelColor = SuperTextSecond
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPhone,
                    onValueChange = { newPhone = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SuperGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SuperGreen,
                        unfocusedBorderColor = SuperBorder,
                        focusedTextColor = SuperTextPrimary,
                        unfocusedTextColor = SuperTextPrimary
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = newCity,
                    onValueChange = { newCity = it },
                    label = { Text("Ciudad") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuperGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SuperGreen,
                        unfocusedBorderColor = SuperBorder,
                        focusedTextColor = SuperTextPrimary,
                        unfocusedTextColor = SuperTextPrimary
                    )
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuperSurface2),
                    border = BorderStroke(1.dp, SuperBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPasswordFields = !showPasswordFields },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Cambiar contraseña",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuperTextPrimary
                            )
                            Icon(
                                imageVector = if (showPasswordFields) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = SuperGreen
                            )
                        }

                        if (showPasswordFields) {
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Contraseña actual") },
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = SuperTextSecond
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuperGreen, unfocusedBorderColor = SuperBorder)
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nueva contraseña") },
                                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(
                                            imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = SuperTextSecond
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuperGreen, unfocusedBorderColor = SuperBorder)
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar contraseña") },
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = SuperTextSecond
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                isError = newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuperGreen, unfocusedBorderColor = SuperBorder)
                            )

                            if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                                Text(
                                    "Las contraseñas no coinciden",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                successMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text(it, color = Color(0xFF2E7D32))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                errorMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text(it, color = Color.Red)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (showPasswordFields && newPassword.isNotBlank() && newPassword != confirmPassword) return@Button
                        scope.launch {
                            currentUserId?.let { uid ->
                                viewModel.updateProfile(
                                    userId = uid,
                                    newUsername = newUsername.takeIf { it != username },
                                    newPhone = newPhone.takeIf { it != phone },
                                    newCity = newCity.takeIf { it != city },
                                    newProfilePicture = profilePicture.takeIf { it != prefs.getProfilePicture() },
                                    currentPassword = currentPassword.takeIf { it.isNotBlank() },
                                    newPassword = newPassword.takeIf { it.isNotBlank() }
                                ) { updatedUsername, updatedProfilePic, updatedPhone, updatedCity ->
                                    scope.launch {
                                        updatedUsername?.let { 
                                            prefs.updateUsername(it)
                                            currentUsername = it
                                            newUsername = it
                                        }
                                        updatedProfilePic?.let { 
                                            prefs.updateProfilePicture(it)
                                            profilePicture = it
                                        }
                                        updatedPhone?.let { 
                                            prefs.updatePhone(it)
                                            phone = it
                                            newPhone = it
                                        }
                                        updatedCity?.let { 
                                            prefs.updateCity(it)
                                            city = it
                                            newCity = it
                                        }
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        showPasswordFields = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = SuperGreen)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar cambios", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(32.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            prefs.clearSession()
                            onBackToLogin()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, SuperBorder)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = SuperTextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", color = SuperTextPrimary)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar cuenta")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text("¿Eliminar cuenta?", color = SuperTextPrimary) },
            text = { Text("Esta acción es permanente. Se eliminarán todos tus datos, listas de compras y favoritos. ¿Estás seguro?", color = SuperTextSecond) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            currentUserId?.let { uid ->
                                viewModel.deleteAccount(
                                    userId = uid,
                                    onSuccess = {
                                        scope.launch {
                                            prefs.clearSession()
                                            onBackToLogin()
                                        }
                                    },
                                    onError = { error -> }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = SuperTextSecond)
                }
            },
            containerColor = SuperSurface
        )
    }
}
