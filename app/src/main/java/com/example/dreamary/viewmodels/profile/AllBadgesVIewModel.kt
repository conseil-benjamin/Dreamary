package com.example.dreamary.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Badge
import com.example.dreamary.models.repositories.DreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllBadgesVIewModel(private val dreamRepository: DreamRepository) : ViewModel() {
    private var _userBadges = MutableStateFlow<List<Badge>>(emptyList())
    var userBadges = _userBadges.asStateFlow()

    fun getUserBadges() {
        viewModelScope.launch{
            dreamRepository.getUserBadgesViewModel().collect { user ->
                _userBadges.value = user
            }
        }
    }
}