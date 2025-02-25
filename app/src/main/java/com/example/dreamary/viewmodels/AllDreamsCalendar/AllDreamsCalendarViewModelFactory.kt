package com.example.dreamary.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.viewmodels.AllDreamsCalendar.AllDreamsCalendarViewModel

class AllDreamsCalendarViewModelFactory(
    private val dreamRepository: DreamRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllDreamsCalendarViewModel::class.java)) {
            return AllDreamsCalendarViewModel(dreamRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}