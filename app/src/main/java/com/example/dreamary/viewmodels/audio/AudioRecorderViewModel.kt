package com.example.dreamary.viewmodels.audio

import android.os.Build
import android.util.Log
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

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _lastDreamDuration = MutableStateFlow(0L)
    val lastDreamDuration: StateFlow<Long> = _lastDreamDuration.asStateFlow()

    fun isMediaPlayerReleased() = audioRecorder.isMediaPlayerReleased()

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
                _audioFilePath.value = audioRecorder.stopRecording()
                _isRecording.value = false
                stopDurationCounter()
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error stopping recording", e)
            }
        }
    }

    fun playAudio() {
        _recordingDuration.value = 0
        audioRecorder.playAudio() { isPlaying ->
            _isPlaying.value = isPlaying
            startDurationCounter()
        }
    }

    fun playAudioFromFirebase(url: String) {
        _recordingDuration.value = 0
        audioRecorder.playAudioFromFirebase(url) { isPlaying ->
            _isPlaying.value = isPlaying
            startDurationCounter()
        }
    }

    fun deleteAudio() {
        viewModelScope.launch {
            try {
                audioRecorder.deleteAudio()
                _audioFilePath.value = null
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error deleting audio", e)
            }
        }
    }

    fun pauseAudio() {
        viewModelScope.launch{
            try {
                Log.i("AudioRecorderViewModel", "Pausing audio")
                audioRecorder.pauseAudio()
                durationJob?.cancel()
                _isPlaying.value = false
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error pausing audio", e)
            }
        }
    }

    fun resumeAudio() {
        viewModelScope.launch {
            try {
                audioRecorder.resumeAudio()
                _isPlaying.value = true
                startDurationCounter()
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error resuming audio", e)
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
                _isRecording.value = false
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
                startDurationCounter()
                _isRecording.value = true
            } catch (e: Exception) {
                // Gérer l'erreur
            }
        }
    }

    fun seekBackward() {
        viewModelScope.launch {
            try {
                audioRecorder.seekBackward()
                _recordingDuration.value -= 5
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error seeking backward", e)
            }
        }
    }

    fun seekForward() {
        viewModelScope.launch {
            try {
                audioRecorder.seekForward()
                _recordingDuration.value += 5
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error seeking forward", e)
            }
        }
    }


    private fun stopDurationCounter() {
        durationJob?.cancel()
        _lastDreamDuration.value = _recordingDuration.value
        _recordingDuration.value = 0
    }

    private fun startDurationCounter() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _recordingDuration.value += 1
            }
        }
    }
}