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

class HomeViewModel(private val repository: DreamRepository): ViewModel() {
    private val _dreams = MutableLiveData<List<Dream>>()
    val dreams: MutableLiveData<List<Dream>> = _dreams

    fun getTwoDreams(userId: String, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            repository.getDreamsForCurrentUser(userId, onFailure = { e ->
                launch {
                    SnackbarManager.showMessage("Erreur lors de la récupération des rêves : $e", R.drawable.error)
                }
            }).collect { dreams ->
                _dreams.value = dreams
                Log.d("Dreams", "Rêves récupérés: $dreams")
            }
        }
    }
}