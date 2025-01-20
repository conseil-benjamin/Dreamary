package com.example.dreamary.viewmodels.auth

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.AuthResponse
import com.example.dreamary.models.states.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.dreamary.utils.SnackbarManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import com.example.dreamary.R

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    fun signInWithGoogle(navController: NavController): Flow<Any> {
        viewModelScope.launch {
            repository.signInWithGoogle(navController)
                .collect { response ->
                    _authState.value = when(response) {
                        is AuthResponse.Success -> AuthState.Authenticated
                        is AuthResponse.Error -> SnackbarManager.showMessage(response.message, R.drawable.error)
                        else -> { AuthState.Initial }
                    } as AuthState
                }
        }
        return flow {
            emit(AuthResponse.Success)
        }
    }

    fun signInWithEmail(context: Context, email: String, password: String, navController: NavController): Flow<Any> {
        viewModelScope.launch {
            if (email.isEmpty() || password.isEmpty()) {
                _authState.value = AuthState.Error("Error")
                val errorMessage = context.getString(R.string.Login_error_message)
                SnackbarManager.showMessage(errorMessage, R.drawable.error)
                return@launch
            }
            repository.signInWithEmail(context, email, password, navController)
                .collect { response ->
                    _authState.value = when(response) {
                        is AuthResponse.Success -> AuthState.Authenticated
                        is AuthResponse.Error -> SnackbarManager.showMessage(response.message, R.drawable.error)
                        else -> { AuthState.Initial }
                    } as AuthState
                }
        }
        return flow {
            emit(AuthResponse.Success)
        }
    }
}