package com.example.dreamary.viewmodels.Social

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.repositories.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatScreenFriendViewModel(private val socialRepository: SocialRepository) : ViewModel() {
    private var _messagesList = MutableStateFlow<List<Message>>(emptyList())
    var messages = _messagesList.asStateFlow()

    fun getMessagesForCurrentUser(chatId: String) {
        viewModelScope.launch {
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
}