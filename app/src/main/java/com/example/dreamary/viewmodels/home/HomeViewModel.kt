package com.example.dreamary.viewmodels.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.utils.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.dreamary.R
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val dreamRepository: DreamRepository, private val authRepository: AuthRepository): ViewModel() {
    private val _dreams = MutableStateFlow<List<Dream>>(emptyList())
    val dreams = _dreams.asStateFlow()
    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    private var _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()


    fun getTwoDreams(userId: String, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            _isLoading.value = true
            dreamRepository.getDreamsForCurrentUser(userId, onFailure = { e ->
                launch {
                    SnackbarManager.showMessage("Erreur lors de la récupération des rêves : $e", R.drawable.error)
                }
            }).collect { dreams ->
                _dreams.value = dreams
                Log.d("Dreams", "Rêves récupérés: $dreams")
            }
            _isLoading.value = false
        }
    }

    fun getProfileData(idUSer : String) {
        viewModelScope.launch{
            _isLoading.value = true
            authRepository.getProfileData(idUSer).collect { user ->
                _userData.value = user
            }
            _isLoading.value = false
        }
    }

    fun updateUserStats(userId: String, field: String, value: Int, coroutineScope: CoroutineScope) {
        viewModelScope.launch {
            authRepository.updateUserStats(
                userId, field, value,
                onSuccess = {
//                    coroutineScope.launch {
//                        SnackbarManager.showMessage("Vous avez perdu votre chaîne", R.drawable.success)
//                    }
                },
                onFailure = {
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Erreur", R.drawable.success)
                    }                }
            ).collect { user ->
                _userData.value = user
            }
        }
    }
}