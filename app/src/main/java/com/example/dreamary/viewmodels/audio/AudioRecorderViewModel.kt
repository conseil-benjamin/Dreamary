package com.example.dreamary.viewmodels.audio

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

class AudioRecorderViewModel(private val audioRecorder: AudioRecorder) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private val _audioFilePath = MutableStateFlow<String?>(null)
    val audioFilePath: StateFlow<String?> = _audioFilePath.asStateFlow()

    private var durationJob: Job? = null

    fun startRecording() {
        viewModelScope.launch {
            try {
                _audioFilePath.value = audioRecorder.startRecording()
                _isRecording.value = true
                startDurationCounter()
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                _audioFilePath.value = audioRecorder.stopRecording().toString()
                _isRecording.value = false
                stopDurationCounter()
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun pauseRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.pauseRecording()
                // Arrêter le compteur de durée
                durationJob?.cancel()
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun resumeRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.resumeRecording()
                // Redémarrer le compteur de durée
                startDurationCounter()
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }


    private fun stopDurationCounter() {
        durationJob?.cancel()
        _recordingDuration.value = 0
    }

    private fun startDurationCounter() {
        durationJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _recordingDuration.value += 1
            }
        }
    }
}