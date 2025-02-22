package com.example.dreamary.viewmodels.dreams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.DreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailsDreamViewModel(private val repository: DreamRepository) : ViewModel() {
    private var _dream = MutableStateFlow<Dream?>(null)
    var dream = _dream.asStateFlow()

    fun getDreamById(idDream: String){
        viewModelScope.launch {
            repository.getDreamById(idDream).collect { dream ->
                _dream.value = dream
            }
        }
    }
}