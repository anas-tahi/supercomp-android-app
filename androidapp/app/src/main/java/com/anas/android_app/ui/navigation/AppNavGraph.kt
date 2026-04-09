package com.anas.android_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anas.android_app.ui.login.LoginScreen
import com.anas.android_app.ui.login.LoginViewModel
import com.anas.android_app.ui.register.RegisterScreen
import com.anas.android_app.ui.register.RegisterViewModel
import com.anas.android_app.ui.screens.home.HomeScreen
import com.anas.android_app.ui.screens.home.HomeViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    homeViewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { username ->
                    navController.navigate("home/$username") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate("register") }
            )
        }

        // REGISTER
        composable("register") {
            RegisterScreen(
                viewModel = RegisterViewModel(),
                onRegisterSuccess = { navController.navigate("login") },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        // HOME
        composable("home/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "User"

            HomeScreen(
                navController = navController,
                username = username,
                homeViewModel = homeViewModel
            )
        }
    }
}
