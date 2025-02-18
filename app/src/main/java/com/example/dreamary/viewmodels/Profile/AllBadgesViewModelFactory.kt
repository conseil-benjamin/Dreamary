package com.example.dreamary.viewmodels.Profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.DreamRepository

class AllBadgesViewModelFactory(
    private val dreamRepository: DreamRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllBadgesVIewModel::class.java)) {
            return AllBadgesVIewModel(dreamRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}