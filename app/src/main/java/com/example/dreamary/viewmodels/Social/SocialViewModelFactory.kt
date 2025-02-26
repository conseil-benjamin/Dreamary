package com.example.dreamary.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.viewmodels.Social.SocialViewModel

class SocialViewModelFactory(
    private val socialRepository: SocialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
            return SocialViewModel(socialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}