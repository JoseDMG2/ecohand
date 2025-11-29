package com.example.ecohand.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Inicio : Screen("inicio")
    object Lecciones : Screen("lecciones")
    object LeccionDetalle : Screen("leccion_detalle/{leccionId}") {
        fun createRoute(leccionId: Int) = "leccion_detalle/$leccionId"
    }
    object LeccionPractica : Screen("leccion_practica/{leccionId}") {
        fun createRoute(leccionId: Int) = "leccion_practica/$leccionId"
    }
    object Progreso : Screen("progreso")
    object Juegos : Screen("juegos")
    object Perfil : Screen("perfil")
    object DataCollection : Screen("data_collection")
}
