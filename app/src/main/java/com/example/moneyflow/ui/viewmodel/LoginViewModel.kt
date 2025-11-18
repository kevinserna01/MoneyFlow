package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.local.TokenManager
import com.example.moneyflow.data.models.LoginResponse
import com.example.moneyflow.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository()
    private val tokenManager = TokenManager(application)
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    fun login(correo: String, contraseña: String) {
        if (correo.isBlank() || contraseña.isBlank()) {
            _loginState.value = LoginState.Error("Correo y contraseña son requeridos")
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            repository.login(correo, contraseña)
                    .onSuccess { loginResponse ->
                        // Guardar token y datos del usuario en DataStore
                        tokenManager.saveToken(loginResponse.token)
                        tokenManager.saveUserId(loginResponse.usuario.id)
                        // Configurar token en ApiClient para futuras peticiones
                        com.example.moneyflow.data.api.ApiClient.setToken(loginResponse.token)
                        val fullName = loginResponse.usuario.nombre.trim()
                        val firstName = fullName
                            .substringBefore(" ")
                            .ifBlank { fullName }
                        tokenManager.saveUserFirstName(firstName)
                        tokenManager.saveUserFullName(fullName)
                        tokenManager.saveUserEmail(loginResponse.usuario.correo)
                        _loginState.value = LoginState.Success(loginResponse)
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Error desconocido")
                }
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val loginResponse: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}
