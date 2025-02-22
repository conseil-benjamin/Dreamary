package com.example.dreamary.viewmodels.dreams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.DreamRepository

class DetailsDreamViewModelFactory(
    private val repository: DreamRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsDreamViewModel::class.java)) {
            return DetailsDreamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}