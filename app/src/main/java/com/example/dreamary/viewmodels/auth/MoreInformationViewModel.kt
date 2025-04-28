package com.example.dreamary.viewmodels.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MoreInformationViewModel (private val repository: AuthRepository) : ViewModel()  {
    private val _profilePictureUri  = MutableStateFlow<String?>(null)
    val profilePictureUri = _profilePictureUri
    private val _loading = MutableStateFlow(false)
    val loading = _loading

    fun uploadProfilePicture(uri: Uri, context: Context) {
        _loading.value = true
        viewModelScope.launch {
            repository.uploadProfilePicture(uri, context)
                .filterNotNull() // permet de filtrer les valeurs nulles
                .collect { validUri ->
                    Log.i("dqzdqzdqzdq", "URI: $validUri")
                    _loading.value = false
                    _profilePictureUri.value = validUri
                }
        }
    }
}