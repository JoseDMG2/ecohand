package com.example.ecohand.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Inicio : BottomNavItem(
        route = Screen.Inicio.route,
        title = "Inicio",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    
    object Lecciones : BottomNavItem(
        route = Screen.Lecciones.route,
        title = "Lecciones",
        icon = Icons.Outlined.Star,
        selectedIcon = Icons.Filled.Star
    )
    
    object Progreso : BottomNavItem(
        route = Screen.Progreso.route,
        title = "Progreso",
        icon = Icons.Outlined.Star,
        selectedIcon = Icons.Filled.Star
    )
    
    object Juegos : BottomNavItem(
        route = Screen.Juegos.route,
        title = "Juegos",
        icon = Icons.Outlined.PlayArrow,
        selectedIcon = Icons.Filled.PlayArrow
    )
    
    object Perfil : BottomNavItem(
        route = Screen.Perfil.route,
        title = "Perfil",
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Inicio,
    BottomNavItem.Lecciones,
    BottomNavItem.Progreso,
    BottomNavItem.Juegos,
    BottomNavItem.Perfil
)
