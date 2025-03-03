package com.example.dreamary.viewmodels.Social

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Conversation
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.entities.Share
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private var _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    var listConversations = _conversations.asStateFlow()

    private var _friendRequests = MutableStateFlow<List<User>>(emptyList())
    var friendRequests = _friendRequests.asStateFlow()

    private var _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()

    private var _friendsAndGroup =  MutableStateFlow<Share>(Share( emptyList(), emptyList()))
    var friendsAndGroup: StateFlow<Share> = _friendsAndGroup

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupsForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getGroupsForCurrentUser(userId).collect { groups ->
                _groupsList.value = groups
            }
        }
    }

    fun getProfileData(userId: String) {
        viewModelScope.launch{
            socialRepository.getProfileData(userId).collect { user ->
                _userData.value = user
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

    fun getConversationsForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getConversationsForCurrentUser(userId).collect { conversations ->
                _conversations.value = conversations
            }
        }
    }

    fun getFriendRequestsForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getFriendRequestsForCurrentUser(userId).collect { friendRequests ->
                _friendRequests.value = friendRequests
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFriendRequest(userId: String, friendId: String, status: String) {
        viewModelScope.launch {
            socialRepository.updateFriendStatus(userId, friendId, status).collect { friendRequests ->
                _friendRequests.value = friendRequests
            }
        }
    }

    fun deleteFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            socialRepository.deleteFriend(userId, friendId).collect { friends ->
                _listFriends.value = friends
            }
        }
    }

    fun createConversation(conversation: Conversation, onConversationCreated: (String) -> Unit) {
        viewModelScope.launch {
            socialRepository.createConversation(conversation).collect { conversations ->
                _conversations.value = conversations
                if (conversations.isNotEmpty()) {
                    Log.i("navigation", "Redirection vers la conversation ${conversations[0].chatId}")
                    onConversationCreated(conversations[0].chatId)
                }
            }
        }
    }

    fun getFriendsAndGroupForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getFriendsAndGroupForCurrentUser(userId)
                .collect { friendsAndGroup ->

                    _friendsAndGroup.value = friendsAndGroup
                }
        }
    }
}