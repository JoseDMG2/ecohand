package com.example.ecohand.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecohand.data.repository.UserRepository
import com.example.ecohand.data.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegistering: Boolean = false,
    val username: String = ""
)

class LoginViewModel(
    private val userRepository: UserRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }
    
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }
    
    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = null)
    }
    
    fun toggleRegistering() {
        _uiState.value = _uiState.value.copy(
            isRegistering = !_uiState.value.isRegistering,
            errorMessage = null
        )
    }
    
    fun login() {
        val currentState = _uiState.value
        
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Por favor complete todos los campos")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val result = userRepository.login(currentState.email, currentState.password)
            
            _uiState.value = if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    userSession.saveUserSession(user.id, user.username, user.email)
                }
                currentState.copy(isLoading = false, isLoginSuccessful = true)
            } else {
                currentState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                )
            }
        }
    }
    
    fun register() {
        val currentState = _uiState.value
        
        if (currentState.username.isBlank() || currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Por favor complete todos los campos")
            return
        }
        
        if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val result = userRepository.registerUser(
                currentState.username,
                currentState.email,
                currentState.password
            )
            
            _uiState.value = if (result.isSuccess) {
                val userId = result.getOrNull()
                if (userId != null) {
                    userSession.saveUserSession(userId.toInt(), currentState.username, currentState.email)
                }
                currentState.copy(isLoading = false, isLoginSuccessful = true)
            } else {
                currentState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar usuario"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
