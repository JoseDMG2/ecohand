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
    object LeccionListaValidacion : Screen("leccion_lista_validacion/{leccionId}") {
        fun createRoute(leccionId: Int) = "leccion_lista_validacion/$leccionId"
    }
    object Progreso : Screen("progreso")
    object Juegos : Screen("juegos")
    object Perfil : Screen("perfil")
    
    // Pantallas de perfil
    object Configuracion : Screen("configuracion")
    object MisLogros : Screen("mis_logros")
    object DiccionarioLSP : Screen("diccionario_lsp")
    object CompartirApp : Screen("compartir_app")
    object AyudaSoporte : Screen("ayuda_soporte")

    // Pantalla de prueba de detección
    object DetectionTest : Screen("detection_test")

    // Pantallas de validación de señas
    object CategorySelection : Screen("category_selection")
    object VowelSelection : Screen("vowel_selection/{category}") {
        fun createRoute(category: String) = "vowel_selection/$category"
    }
    object VowelValidation : Screen("vowel_validation/{vowel}?leccionId={leccionId}") {
        fun createRoute(vowel: String, leccionId: Int? = null): String {
            val leccionIdParam = leccionId ?: -1
            return "vowel_validation/$vowel?leccionId=$leccionIdParam"
        }
    }
}
