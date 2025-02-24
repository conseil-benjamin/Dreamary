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

    fun playAudio() {
       mediaPlayer = MediaPlayer().apply {
            setDataSource(currentFilePath)
            prepare()
            start()
        }
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun playAudioFromFirebase(url: String, onReady: (Boolean) -> Unit) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    onReady(true) // Audio prêt
                }

                setOnCompletionListener {
                    release()
                    onReady(false) // Audio terminé
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error playing audio", e)
            onReady(false) // En cas d'erreur
        }
    }


    fun deleteAudio() {
        val file = File(currentFilePath)
        if (file.exists()) {
            file.delete()
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