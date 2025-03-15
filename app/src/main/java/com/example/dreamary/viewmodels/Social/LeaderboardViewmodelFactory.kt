package com.example.dreamary.viewmodels.Social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.viewmodels.Social.SocialViewModel

class LeaderboardViewmodelFactory(
    private val socialRepository: SocialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewmodel::class.java)) {
            return LeaderboardViewmodel(socialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}