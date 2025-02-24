package com.example.dreamary.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val dreamRepository: DreamRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(authRepository, dreamRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}