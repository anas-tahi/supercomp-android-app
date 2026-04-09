package com.anas.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.anas.android_app.data.local.UserPrefs
import com.anas.android_app.data.remote.ApiServiceInstance
import com.anas.android_app.ui.screens.home.HomeViewModel
import com.anas.android_app.ui.login.LoginViewModel
import com.anas.android_app.ui.login.LoginViewModelFactory
import com.anas.android_app.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = UserPrefs(applicationContext)

        val loginViewModel = LoginViewModelFactory(
            prefs,
            ApiServiceInstance.api
        ).create(LoginViewModel::class.java)

        setContent {
            val navController = rememberNavController()

            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavGraph(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
}
