package com.example.ecohand.data.session

object LeccionValidacionState {
    private val senasCompletadas = mutableMapOf<Pair<Int, String>, Boolean>()

    fun marcarSenaCompletada(leccionId: Int, letra: String) {
        senasCompletadas[Pair(leccionId, letra)] = true
    }

    fun estaSenaCompletada(leccionId: Int, letra: String): Boolean {
        return senasCompletadas[Pair(leccionId, letra)] ?: false
    }

    fun limpiarLeccion(leccionId: Int) {
        senasCompletadas.keys.removeAll { it.first == leccionId }
    }

    fun limpiarTodo() {
        senasCompletadas.clear()
    }
}

