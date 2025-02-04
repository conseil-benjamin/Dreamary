package com.example.dreamary.viewmodels.audio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioRecorderViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioRecorderViewModel::class.java)) {
            return AudioRecorderViewModel(AudioRecorder(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}