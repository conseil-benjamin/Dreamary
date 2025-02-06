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
        mediaRecorder?.apply {
            resume()
        }
    }

    fun stopRecording(): String {
        val filePath = currentFilePath
        var storage = Firebase.storage

        mediaRecorder?.apply {
            stop()
            release()
        }

//        val audioRef = storageRef.child("audio/${currentUser?.uid}/dream_${System.currentTimeMillis()}.mp3")
//
//        val uriFile = Uri.fromFile(file)
//        val uploadTask = audioRef.putFile(uriFile)
//
//        uploadTask.addOnSuccessListener { taskSnapshot ->
//            // Obtenir l'URL de téléchargement
//            audioRef.downloadUrl.addOnSuccessListener { uri ->
//                Log.d("AudioRecorder", "Fichier audio téléchargé avec succès à l'URL: $uri")
//            }
//        }.addOnFailureListener { exception ->
//            // Gérer les erreurs
//            Log.e("AudioRecorder", "Erreur lors du téléchargement du fichier audio", exception)
//        }.addOnProgressListener { taskSnapshot ->
//            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
//            // Mettre à jour la progression si nécessaire
//        }
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