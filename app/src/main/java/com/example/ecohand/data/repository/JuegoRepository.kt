package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.PartidaJuegoDao
import com.example.ecohand.data.local.dao.SenaDao
import com.example.ecohand.data.local.dao.EstadisticasUsuarioDao
import com.example.ecohand.data.local.entity.PartidaJuegoEntity
import com.example.ecohand.data.local.entity.SenaEntity

class JuegoRepository(
    private val senaDao: SenaDao,
    private val partidaJuegoDao: PartidaJuegoDao,
    private val estadisticasUsuarioDao: EstadisticasUsuarioDao
) {

    // Obtener señas aleatorias sin repetir
    suspend fun getSenasAleatorias(cantidad: Int): List<SenaEntity> {
        return senaDao.getSenasAleatorias(cantidad)
    }

    // Obtener todas las señas
    suspend fun getAllSenas(): List<SenaEntity> {
        return senaDao.getAllSenas()
    }

    // Crear nueva partida
    suspend fun crearPartida(usuarioId: Int): Long {
        val partida = PartidaJuegoEntity(usuarioId = usuarioId)
        return partidaJuegoDao.insertPartida(partida)
    }

    // Actualizar progreso de partida
    suspend fun actualizarPartida(partida: PartidaJuegoEntity) {
        partidaJuegoDao.updatePartida(partida)
    }

    // Completar partida y actualizar estadísticas
    suspend fun completarPartida(partidaId: Int, usuarioId: Int) {
        val partida = partidaJuegoDao.getPartidaById(partidaId)
        partida?.let {
            // Marcar partida como completada
            val partidaCompleta = it.copy(
                completada = true,
                fechaFin = System.currentTimeMillis()
            )
            partidaJuegoDao.updatePartida(partidaCompleta)

            // Actualizar estadísticas del usuario
            val estadisticas = estadisticasUsuarioDao.getEstadisticasByUsuario(usuarioId)
            estadisticas?.let { stats ->
                estadisticasUsuarioDao.updateEstadisticas(
                    stats.copy(
                        puntosTotal = stats.puntosTotal + partidaCompleta.puntosGanados,
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Obtener partidas del usuario
    suspend fun getPartidasUsuario(usuarioId: Int): List<PartidaJuegoEntity> {
        return partidaJuegoDao.getPartidasByUsuario(usuarioId)
    }

    // Obtener total de partidas completadas
    suspend fun getPartidasCompletadas(usuarioId: Int): Int {
        return partidaJuegoDao.getPartidasCompletadas(usuarioId)
    }
}

