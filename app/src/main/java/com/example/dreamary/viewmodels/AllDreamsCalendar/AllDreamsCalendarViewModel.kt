package com.example.dreamary.viewmodels.AllDreamsCalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllDreamsCalendarViewModel(private val dreamRepository: DreamRepository): ViewModel() {
    private var _dreams = MutableStateFlow(emptyList<Dream>())
    var dreams = _dreams.asStateFlow()

//    fun getDreams(userId: String){ {
//        viewModelScope.launch{
//            dreamRepository.getDreamsForCurrentUser(userId, onFailure =).collect{ dreams ->
//                _dreams.value = dreams
//            }
//        }
//    }
}