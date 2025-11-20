package com.example.ecohand.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ecohand.data.local.database.EcoHandDatabase
import com.example.ecohand.data.repository.JuegoRepository
import com.example.ecohand.data.repository.ProgresoRepository
import com.example.ecohand.data.session.UserSession
import com.example.ecohand.navigation.Screen
import com.example.ecohand.navigation.bottomNavItems
import com.example.ecohand.presentation.home.InicioScreen
import com.example.ecohand.presentation.juegos.JuegosScreen
import com.example.ecohand.presentation.juegos.JuegosViewModel
import com.example.ecohand.presentation.lecciones.LeccionesScreen
import com.example.ecohand.presentation.perfil.PerfilScreen
import com.example.ecohand.presentation.progreso.ProgresoScreen
import com.example.ecohand.presentation.progreso.ProgresoViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicializar base de datos y repositorios
    val database = EcoHandDatabase.getDatabase(context)
    val userSession = UserSession.getInstance(context)
    val usuarioId = userSession.getUserId()

    // Crear ProgresoRepository
    val progresoRepository = ProgresoRepository(
        estadisticasUsuarioDao = database.estadisticasUsuarioDao(),
        progresoLeccionDao = database.progresoLeccionDao(),
        actividadDiariaDao = database.actividadDiariaDao(),
        logroDao = database.logroDao(),
        logroUsuarioDao = database.logroUsuarioDao(),
        leccionDao = database.leccionDao()
    )

    // Crear ProgresoViewModel con remember para evitar recreaciones
    val progresoViewModel = remember(usuarioId) {
        ProgresoViewModel(progresoRepository, usuarioId)
    }

    // Crear JuegoRepository
    val juegoRepository = JuegoRepository(
        senaDao = database.senaDao(),
        partidaJuegoDao = database.partidaJuegoDao(),
        estadisticasUsuarioDao = database.estadisticasUsuarioDao()
    )

    // Crear JuegosViewModel con remember para evitar recreaciones
    val juegosViewModel = remember(usuarioId) {
        JuegosViewModel(juegoRepository, usuarioId)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            progresoViewModel = progresoViewModel,
            juegosViewModel = juegosViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    progresoViewModel: ProgresoViewModel,
    juegosViewModel: JuegosViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inicio.route,
        modifier = modifier
    ) {
        composable(Screen.Inicio.route) {
            InicioScreen()
        }
        composable(Screen.Lecciones.route) {
            LeccionesScreen()
        }
        composable(Screen.Progreso.route) {
            ProgresoScreen(viewModel = progresoViewModel)
        }
        composable(Screen.Juegos.route) {
            JuegosScreen(viewModel = juegosViewModel)
        }
        composable(Screen.Perfil.route) {
            PerfilScreen()
        }
    }
}
