package com.example.dreamary.viewmodels.Social

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatScreenFriendViewModel(private val socialRepository: SocialRepository) : ViewModel() {
    private var _messagesList = MutableStateFlow<List<Message>>(emptyList())
    var messages = _messagesList.asStateFlow()

    private var _friendInformation = MutableStateFlow<User?>(null)
    var friendInformation = _friendInformation.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    fun getMessagesForCurrentUser(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            socialRepository.getMessagesForCurrentUser(chatId).collect { messages ->
                _messagesList.value = messages
            }
        }
    }

    fun sendMessage(chatId: String, message: Message) {
        Log.i("sendMessage", message.toString())
        viewModelScope.launch {
            socialRepository.sendMessage(chatId, message)
        }
    }

    fun getFriendInformation(userId: String) {
        viewModelScope.launch {
            socialRepository.getProfileData(userId).collect { user ->
                _friendInformation.value = user
                _isLoading.value = false
            }
        }
    }
}