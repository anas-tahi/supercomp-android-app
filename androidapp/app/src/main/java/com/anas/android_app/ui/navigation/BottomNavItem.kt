package com.anas.android_app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object CompareItem : BottomNavItem("compare", Icons.Filled.Compare, "Compare")
    object ListCompare : BottomNavItem("list_compare", Icons.Filled.List, "List")
    object Favorites : BottomNavItem("favorites", Icons.Filled.Favorite, "Favorites")
    object Profile : BottomNavItem("profile", Icons.Filled.Person, "Profile")
}
