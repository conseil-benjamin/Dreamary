package com.example.dreamary.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.AuthResponse
import com.example.dreamary.models.states.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class RegisterViewModel (private val repository: AuthRepository) : ViewModel()  {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    fun createAccountWithEmail(email: String, password: String, navController: NavController): Flow<Any> {
        viewModelScope.launch {
            repository.createAccountWithEmail(email, password, navController)
                .collect { response ->
                    _authState.value = when(response) {
                        is AuthResponse.Success -> AuthState.Authenticated
                        is AuthResponse.Error -> AuthState.Error(response.message)
                        else -> { AuthState.Initial}
                    }
                }
        }
        return flow {
            emit(AuthResponse.Success)
        }
    }

    fun signUpWithGoogle(navController: NavController): Flow<Any> {
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
        return flow {
            emit(AuthResponse.Success)
        }
    }
}