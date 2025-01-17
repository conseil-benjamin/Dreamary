package com.example.dreamary.viewmodels.auth

import androidx.navigation.NavHostController
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.AuthResponse
import com.example.dreamary.models.states.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    fun signInWithGoogle(navController: NavHostController) {
        viewModelScope.launch {
            repository.signInWithGoogle(navController)
                .collect { response ->
                    _authState.value = when(response) {
                        is AuthResponse.Success -> AuthState.Authenticated
                        is AuthResponse.Error -> AuthState.Error(response.message)
                        else -> { AuthState.Initial }
                    }
                }
        }
    }

    fun createAccountWithEmail(email: String, password: String, navController: NavHostController) {
        viewModelScope.launch {
            repository.createAccountWithEmail(email, password)
                .collect { response ->
                    _authState.value = when(response) {
                        is AuthResponse.Success -> AuthState.Authenticated
                        is AuthResponse.Error -> AuthState.Error(response.message)
                        else -> { AuthState.Initial}
                    }
                }
        }
    }
}