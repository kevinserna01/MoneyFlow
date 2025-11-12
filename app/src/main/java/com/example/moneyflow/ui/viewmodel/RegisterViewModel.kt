package com.example.moneyflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.models.Usuario
import com.example.moneyflow.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository()
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()
    
    fun register(
        nombre: String,
        telefono: String,
        correo: String,
        contraseña: String,
        confirmarContraseña: String
    ) {
        // Validaciones básicas
        if (nombre.isBlank() || telefono.isBlank() || correo.isBlank() || 
            contraseña.isBlank() || confirmarContraseña.isBlank()) {
            _registerState.value = RegisterState.Error("Todos los campos son requeridos")
            return
        }
        
        if (contraseña.length < 6) {
            _registerState.value = RegisterState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        if (contraseña != confirmarContraseña) {
            _registerState.value = RegisterState.Error("Las contraseñas no coinciden")
            return
        }
        
        if (telefono.length < 8) {
            _registerState.value = RegisterState.Error("El teléfono debe tener al menos 8 caracteres")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _registerState.value = RegisterState.Error("El formato del correo electrónico no es válido")
            return
        }
        
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            repository.createUsuario(nombre, telefono, correo, contraseña)
                .onSuccess { usuario ->
                    _registerState.value = RegisterState.Success(usuario)
                }
                .onFailure { error ->
                    _registerState.value = RegisterState.Error(error.message ?: "Error desconocido")
                }
        }
    }
    
    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val usuario: Usuario) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
