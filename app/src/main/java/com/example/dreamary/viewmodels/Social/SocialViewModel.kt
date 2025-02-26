package com.example.dreamary.viewmodels.Social

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialViewModel(private val socialRepository: SocialRepository) : ViewModel() {
    private var _groupsList = MutableStateFlow<List<Group>>(emptyList())
    var groups = _groupsList.asStateFlow()
    val db = FirebaseFirestore.getInstance()
    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    private var _usersList = MutableStateFlow<List<User>>(emptyList())
    var users = _usersList.asStateFlow()

    private var _listFriends = MutableStateFlow<List<User>>(emptyList())
    var listFriends = _listFriends.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupsForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getGroupsForCurrentUser(userId).collect { groups ->
                _groupsList.value = groups
            }
        }
    }

    fun getFriendsForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getFriendsForCurrentUser(userId).collect { friends ->
                _listFriends.value = friends
            }
        }
    }

    fun searchUsers(searchValue: String) {
        if (searchValue.isEmpty()) {
            _usersList.value = emptyList()
            Log.i("search", users.toString())
            return
        }
        viewModelScope.launch {
            socialRepository.searchUsers(searchValue).collect { users ->
                _usersList.value = users
            }
        }
    }
}