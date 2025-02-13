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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val dreamRepository: DreamRepository, private val authRepository: AuthRepository): ViewModel() {
    private val _dreams = MutableLiveData<List<Dream>>()
    val dreams: MutableLiveData<List<Dream>> = _dreams
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
                _isLoading.value = false
                _dreams.value = dreams
                Log.d("Dreams", "Rêves récupérés: $dreams")
            }
            _isLoading.value = false
        }
    }

    fun getProfileData(idUSer : String) {
        viewModelScope.launch{
            authRepository.getProfileData(idUSer).collect { user ->
                _userData.value = user
            }
        }
    }
}