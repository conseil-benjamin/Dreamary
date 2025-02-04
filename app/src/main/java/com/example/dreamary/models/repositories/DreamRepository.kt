package com.example.dreamary.models.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.states.DreamResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DreamRepository(private val context: Context) {
    val db = Firebase.firestore

    fun addDream(dream: Dream, onSuccess: () -> Unit, onFailure: (Exception) -> Unit): Flow<DreamResponse> = flow {
        try {
            if (dream.audio["path"] != ""){
                val filePath = dream.audio["path"] as String
                var storage = Firebase.storage
                val storageRef = storage.reference
                val currentUser = Firebase.auth.currentUser
                val file = File(filePath)
                val audioRef = storageRef.child("audio/${currentUser?.uid}/dream_${System.currentTimeMillis()}.mp3")

                val uriFile = Uri.fromFile(file)
                val uploadTask = audioRef.putFile(uriFile)
                Log.i("filePath", filePath)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // Obtenir l'URL de téléchargement
                    audioRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("AudioRecorder", "Fichier audio téléchargé avec succès à l'URL: $uri")
                        Log.i("uri", uri.toString())
                        (dream.audio as MutableMap<String, Any>).put("url", uri.toString())
                    }
                }.addOnFailureListener { exception ->
                    // Gérer les erreurs
                    Log.e("AudioRecorder", "Erreur lors du téléchargement du fichier audio", exception)
                }.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                }
            } else {
                Log.i("filePath", "No audio file")
            }

            db.collection("dreams")
                .add(dream)
                .addOnSuccessListener { documentReference ->
                    println("DocumentSnapshot added with ID: ${documentReference.id}")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                    onFailure(e)
                }
        } catch (
            e: Exception
        ) {
            emit(DreamResponse.Error(e.message ?: "Une erreur est survenue"))
        }
    }
}