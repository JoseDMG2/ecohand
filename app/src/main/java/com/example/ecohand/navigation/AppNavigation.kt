package com.example.ecohand.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.repository.UserRepository
import com.example.ecohand.data.session.UserSession
import com.example.ecohand.presentation.login.LoginScreen
import com.example.ecohand.presentation.login.LoginViewModel
import com.example.ecohand.presentation.main.MainScreen
import com.example.ecohand.presentation.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Inicializar base de datos, repositorio y sesión
    val database = remember { EcoHandDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val userSession = remember { UserSession.getInstance(context) }
    val loginViewModel = remember { LoginViewModel(userRepository, userSession) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            MainScreen()
        }
    }
}
