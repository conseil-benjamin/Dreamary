package com.example.dreamary.viewmodels.stats

import com.example.dreamary.viewmodels.home.HomeViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository

class StatsViewModelFactory(
    private val dreamRepository: DreamRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(dreamRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}