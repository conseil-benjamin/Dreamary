package com.example.dreamary.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository): ViewModel() {
    private var _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()

    fun getProfileData(idUSer : String) {
        viewModelScope.launch{
            repository.getProfileData(idUSer).collect { user ->
                _userData.value = user
            }
        }
    }
}