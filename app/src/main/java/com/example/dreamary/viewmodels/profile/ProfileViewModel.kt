package com.example.dreamary.viewmodels.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Badge
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository,private val dreamRepository: DreamRepository): ViewModel() {
    private var _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()
    private var _userBadges = MutableStateFlow<List<Badge>>(emptyList())
    var userBadges = _userBadges.asStateFlow()

    private val _friend = MutableStateFlow<String?>(null)
    var friend = _friend.asStateFlow()

    fun getProfileData(idUSer : String) {
        viewModelScope.launch{
            repository.getProfileData(idUSer).collect { user ->
                _userData.value = user
            }
        }
    }

    fun getUserBadges() {
        viewModelScope.launch{
            Log.d("ProfileViewModel", "Lancement")
            dreamRepository.getUserBadgesViewModel().collect { user ->
                _userBadges.value = user
                Log.d("ProfileViewModel", "User badges: $user")
            }
        }
    }

    fun verifyIfWeAreFriends(idUser: String, idFriend: String) {
        viewModelScope.launch {
            repository.verifyIfWeAreFriends(idUser, idFriend).collect { user ->
                _friend.value = user
            }
        }
    }

    fun sendFriendRequest(idUser: String, idFriend: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(idUser, idFriend)
        }
    }

}