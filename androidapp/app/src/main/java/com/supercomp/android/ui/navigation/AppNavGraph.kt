package com.supercomp.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.supercomp.android.ui.auth.login.LoginScreen
import com.supercomp.android.ui.auth.register.RegisterScreen
import com.supercomp.android.ui.screens.compare.CompareScreen
import com.supercomp.android.ui.screens.favorites.FavoritesScreen
import com.supercomp.android.ui.screens.home.HomeScreen
import com.supercomp.android.ui.screens.profile.ProfileScreen
import com.supercomp.android.ui.screens.shoppinglist.ShoppingListScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { username, userId ->
                    navController.navigate("home/$username/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = "home/{username}/{userId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("userId")   { type = NavType.StringType }
            )
        ) { back ->
            HomeScreen(navController, back.arguments?.getString("username") ?: "", back.arguments?.getString("userId") ?: "")
        }

        composable(
            route = "compare/{username}/{userId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("userId")   { type = NavType.StringType }
            )
        ) { back ->
            CompareScreen(navController, back.arguments?.getString("username") ?: "", back.arguments?.getString("userId") ?: "")
        }

        composable(
            route = "favorites/{username}/{userId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("userId")   { type = NavType.StringType }
            )
        ) { back ->
            FavoritesScreen(navController, back.arguments?.getString("username") ?: "", back.arguments?.getString("userId") ?: "")
        }

        composable(
            route = "shoppinglist/{username}/{userId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("userId")   { type = NavType.StringType }
            )
        ) { back ->
            ShoppingListScreen(navController, back.arguments?.getString("username") ?: "", back.arguments?.getString("userId") ?: "")
        }

        composable(
            route = "profile/{username}/{userId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("userId")   { type = NavType.StringType }
            )
        ) { back ->
            ProfileScreen(navController, back.arguments?.getString("username") ?: "", back.arguments?.getString("userId") ?: "")
        }
    }
}
