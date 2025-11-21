package com.example.ecohand.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Inicio : Screen("inicio")
    object Lecciones : Screen("lecciones")
    object Progreso : Screen("progreso")
    object Juegos : Screen("juegos")
    object Perfil : Screen("perfil")
    
    // Pantallas de perfil
    object Configuracion : Screen("configuracion")
    object MisLogros : Screen("mis_logros")
    object DiccionarioLSP : Screen("diccionario_lsp")
    object CompartirApp : Screen("compartir_app")
    object AyudaSoporte : Screen("ayuda_soporte")
}
