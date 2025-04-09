package com.example.dreamary.viewmodels.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import java.io.File

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val fileName = "audio_${System.currentTimeMillis()}.mp3"
    private var currentFilePath = "${context.filesDir.absolutePath}/$fileName"
    private var currentPositionLocal = 0

    fun isMediaPlayerReleased(): Boolean {
        return mediaPlayer == null
    }

    fun startRecording(): String {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(currentFilePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AudioRecorder", "Erreur lors du démarrage de l'enregistrement", e)
                throw e
            }
        }
        return currentFilePath
    }

    fun pauseRecording() {
        mediaRecorder?.apply {
            pause()
        }
    }

    fun pauseAudio() {
        Log.i("AudioRecorder", "pauseAudio() called")
        if (mediaPlayer == null) {
            Log.e("AudioRecorder", "mediaPlayer is null")
        } else {
            mediaPlayer?.apply {
                pause()
                currentPositionLocal = currentPosition
            }
        }
    }

    fun resumeAudio() {
        Log.i("AudioRecorder", currentPositionLocal.toString())
        mediaPlayer?.apply {
            mediaPlayer!!.seekTo(currentPositionLocal)
            start()
        }
    }

    fun resumeRecording() {
        Log.i("AudioRecorder", "Resuming recording")
        mediaRecorder?.apply {
            resume()
        }
    }

    fun stopRecording(): String {
        val filePath = currentFilePath
        Log.i("AudioRecorder", "Fichier audio enregistré à $filePath")

        mediaRecorder?.apply {
            stop()
            release()
        }

        return filePath
    }

    fun cancelRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
    }

    fun playAudio(onReady: (Boolean) -> Unit) {
        try {
             mediaPlayer = MediaPlayer().apply {
                setDataSource(currentFilePath)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    onReady(true)
                }

                setOnCompletionListener {
                    mediaPlayer = null
                    onReady(false)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error playing audio", e)
            onReady(false) // En cas d'erreur
        }
    }

    fun playAudioFromFirebase(url: String, onReady: (Boolean) -> Unit) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    onReady(true) // Audio prêt
                }

                setOnCompletionListener {
                    mediaPlayer = null
                    onReady(false) // Audio terminé
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error playing audio", e)
            onReady(false) // En cas d'erreur
        }
    }

    fun seekBackward() {
        mediaPlayer?.apply {
            val currentPosition = currentPosition
            if (currentPosition > 5000) { // Ne pas reculer de plus de 5 secondes
                Log.i("AudioRecorder", "Seeking backward")
                seekTo(currentPosition - 5000)
                start()
            } else {
                Log.i("AudioRecorder", "Noooooo Seeking backward")
                seekTo(0)
            }
        }
    }

    fun seekForward() {
        mediaPlayer?.apply {
            val currentPosition = currentPosition
            Log.i("AudioRecorder", "Current position: $currentPosition")
            if (currentPosition < duration - 5000) {
                Log.i("AudioRecorder", "Seeking forward")
                seekTo(currentPosition + 5000)
                start()
            } else {
                Log.i("AudioRecorder", duration.toString())
                seekTo(duration)
            }
        }
    }


    fun deleteAudio() {
        val file = File(currentFilePath)
        if (file.exists()) {
            file.delete()
            mediaPlayer = null
        }
    }

    fun isRecording(): Boolean {
        return mediaRecorder != null
    }

    fun getFilePath(): String {
        return context.filesDir.absolutePath + "/audio.3gp"
    }

    fun deleteFile() {
        context.deleteFile("audio.3gp")
    }

    fun getDuration(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }
}