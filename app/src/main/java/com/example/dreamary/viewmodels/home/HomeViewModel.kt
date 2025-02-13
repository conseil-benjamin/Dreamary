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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val repository: DreamRepository): ViewModel() {
    private val _dreams = MutableLiveData<List<Dream>>()
    val dreams: MutableLiveData<List<Dream>> = _dreams
    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    fun getTwoDreams(userId: String, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            _isLoading.value = true
            repository.getDreamsForCurrentUser(userId, onFailure = { e ->
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
}