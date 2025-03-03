package com.example.dreamary.viewmodels.dreams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.repositories.SocialRepository

class AddDreamViewModelFactory(
    private val repository: DreamRepository,
    private val socialRepository: SocialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddDreamViewModel::class.java)) {
            return AddDreamViewModel(repository, socialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}