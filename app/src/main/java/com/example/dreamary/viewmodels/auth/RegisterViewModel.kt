package com.example.dreamary.viewmodels.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.AuthResponse
import com.example.dreamary.models.states.AuthState
import com.example.dreamary.utils.SnackbarManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class RegisterViewModel (private val repository: AuthRepository) : ViewModel()  {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()
    var errorMessage = ""


    // TODO : Renvoyer un message d'erreur personnalisé dans la snackbar
    // TODO : vérifier si la checkbox est cochée
    fun checkValidForm(email: String, password: String, confirmPassword: String, cguAccepted: Boolean): Boolean {
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return false
        }

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Error")
            return false
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return false
        }
        if (!email.contains("@") || !email.contains(".") || email.length < 5) {
            _authState.value = AuthState.Error("Invalid email address")
            return false
        }

        if (!cguAccepted) {
            _authState.value = AuthState.Error("You must accept the terms and conditions")
            return false
        }

        return true
    }

    fun createAccountWithEmail(
        context: Context,
        email: String,
        password: String,
        confirmPassword: String,
        navController: NavController,
        isRulesAccepted: Boolean,
        name: String
    ): Flow<Any> {
        viewModelScope.launch {
            if (checkValidForm(email, password, confirmPassword, isRulesAccepted) != true) {
                SnackbarManager.showMessage(context.getString(R.string.Register_error_message), R.drawable.error)
                return@launch
            }

            repository.createAccountWithEmail(context, email, password, navController, name)
                .collect { response ->
                    _authState.value = when(response) {
                        is AuthResponse.Success -> {
                            // Envoyer le message de succès
                            SnackbarManager.showMessage("", R.drawable.success)
                            // Retourner l'état authentifié
                            AuthState.Authenticated
                        }
                        is AuthResponse.Error -> {
                            SnackbarManager.showMessage(response.message, R.drawable.error)
                            AuthState.Error(response.message)
                        }
                        else -> AuthState.Initial
                    }
                }
        }

        return flow {
            emit(AuthResponse.Success)
        }
    }

    fun signUpWithGoogle(navController: NavController): Flow<Any> {
        viewModelScope.launch {
            repository.signInWithGoogle(navController, true)
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