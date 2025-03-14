package com.example.dreamary.viewmodels.AllDreamsCalendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.utils.SnackbarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllDreamsCalendarViewModel(private val dreamRepository: DreamRepository, private val authRepository: AuthRepository): ViewModel() {
    private var _dreams = MutableStateFlow(emptyList<Dream>())
    var dreams = _dreams.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun getAllDreamsForUser(userId: String, coroutineScope: CoroutineScope) {
            viewModelScope.launch {
                dreamRepository.getAllDreamsForUser(userId, onFailure = {
                    coroutineScope.launch{
                            SnackbarManager.showMessage("Erreur lors de la récupération des rêves", SnackbarType.ERROR)
                        }
                }).collect { dreams ->
                    Log.d("Dreams5", "Rêves récupérés: $dreams")
                    _dreams.value = dreams
                }
            }
    }

    fun getProfileData(idUSer : String) {
        viewModelScope.launch{
            authRepository.getProfileData(idUSer).collect { user ->
                _userData.value = user
                Log.d("User", "Utilisateur récupéré: $user")
            }
        }
    }
}