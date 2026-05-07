package com.supercomp.android.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.supercomp.android.ui.theme.SuperGreen
import com.supercomp.android.ui.theme.SuperNavy
import com.supercomp.android.ui.theme.SuperSurface
import com.supercomp.android.ui.theme.SuperTextSecond

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BottomBar(navController: NavController, username: String, userId: String) {
    val items = listOf(
        BottomNavItem("home/$username/$userId",         "Home",      Icons.Filled.Home),
        BottomNavItem("compare/$username/$userId",      "Compare",   Icons.Filled.Search),
        BottomNavItem("favorites/$username/$userId",    "Saved",     Icons.Filled.Favorite),
        BottomNavItem("shoppinglist/$username/$userId", "Lists",     Icons.Filled.List),
        BottomNavItem("profile/$username/$userId",      "Profile",   Icons.Filled.Person),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = SuperSurface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute?.startsWith(item.route.substringBefore("/")) == true

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label,
                        modifier           = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(item.label, fontSize = 10.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = SuperGreen,
                    selectedTextColor   = SuperGreen,
                    unselectedIconColor = SuperTextSecond,
                    unselectedTextColor = SuperTextSecond,
                    indicatorColor      = SuperGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}