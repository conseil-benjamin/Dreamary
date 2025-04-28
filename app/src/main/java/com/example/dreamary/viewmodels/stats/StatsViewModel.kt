package com.example.dreamary.viewmodels.stats

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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class StatsViewModel (private val dreamRepository: DreamRepository, private val authRepository: AuthRepository): ViewModel() {
    private var _dreams = MutableStateFlow(emptyList<Dream>())
    var dreams = _dreams.asStateFlow()

    private var _user = MutableStateFlow<User?>(null)
    var user = _user.asStateFlow()

    private var _loading = MutableStateFlow(false)
    var loading = _loading.asStateFlow()

    fun getAllDreamsForUser(userId: String, coroutineScope: CoroutineScope) {
        viewModelScope.launch {
            dreamRepository.getAllDreamsForUser(userId, onFailure = {
                coroutineScope.launch{
                    SnackbarManager.showMessage("Erreur lors de la récupération des rêves", SnackbarType.ERROR)
                }
            })
                .filterNotNull()
                .collect{ dreams ->
                Log.d("Dreams5", "Rêves récupérés: $dreams")
                _dreams.value = dreams
                _loading.value = true
            }
        }
    }

    fun getProfileData(idUSer : String) {
        viewModelScope.launch{
            authRepository.getProfileData(idUSer).collect { user ->
                _user.value = user
                Log.d("User", "Utilisateur récupéré: $user")
            }
        }
    }
}