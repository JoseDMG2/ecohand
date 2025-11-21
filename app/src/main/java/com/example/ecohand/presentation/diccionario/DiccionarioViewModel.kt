package com.example.ecohand.presentation.diccionario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.local.entity.SenaEntity
import com.example.ecohand.data.repository.DiccionarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiccionarioUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val senas: List<SenaEntity> = emptyList(),
    val searchQuery: String = "",
    val senasAgrupadas: Map<Char, List<SenaEntity>> = emptyMap()
)

class DiccionarioViewModel(
    private val diccionarioRepository: DiccionarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DiccionarioUiState())
    val uiState: StateFlow<DiccionarioUiState> = _uiState.asStateFlow()
    
    init {
        // Cargar datos de forma lazy/diferida
        viewModelScope.launch(Dispatchers.IO) {
            cargarSenas()
        }
    }
    
    /**
     * Carga todas las señas del diccionario
     */
    private suspend fun cargarSenas() {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val senas = diccionarioRepository.getAllSenas()
            val senasAgrupadas = agruparSenasPorLetra(senas)
            
            _uiState.value = DiccionarioUiState(
                isLoading = false,
                senas = senas,
                senasAgrupadas = senasAgrupadas
            )
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Error al cargar el diccionario: ${e.message}"
            )
        }
    }
    
    /**
     * Busca señas según el query
     */
    fun buscarSenas(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(searchQuery = query)
                
                val senas = diccionarioRepository.searchSenas(query)
                val senasAgrupadas = agruparSenasPorLetra(senas)
                
                _uiState.value = _uiState.value.copy(
                    senas = senas,
                    senasAgrupadas = senasAgrupadas,
                    errorMessage = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al buscar: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Agrupa las señas por su letra inicial
     */
    private fun agruparSenasPorLetra(senas: List<SenaEntity>): Map<Char, List<SenaEntity>> {
        return senas.groupBy { 
            it.nombre.firstOrNull()?.uppercaseChar() ?: 'A' 
        }.toSortedMap()
    }
    
    /**
     * Limpia la búsqueda
     */
    fun limpiarBusqueda() {
        buscarSenas("")
    }
}
