package com.example.dreamary.viewmodels.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MoreInformationViewModel (private val repository: AuthRepository) : ViewModel()  {
    private val _profilePictureUri  = MutableStateFlow<String?>(null)
    val profilePictureUri = _profilePictureUri

    fun uploadProfilePicture(uri: Uri, context: Context) {
        viewModelScope.launch {
            repository.uploadProfilePicture(uri, context).collect { uri ->
                _profilePictureUri.value = uri
            }
        }
    }
}