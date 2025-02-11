package com.example.dreamary.models.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.states.DreamResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File

class DreamRepository(private val context: Context) {
    private val db = Firebase.firestore

    fun addDream(dream: Dream, onSuccess: () -> Unit, onFailure: (Exception) -> Unit): Flow<DreamResponse> = flow {
        try {
            if (dream.audio["path"] != "") {
                Log.i("filePath", dream.audio.toString())
                val filePath = dream.audio["path"] as String
                var storage = Firebase.storage
                val storageRef = storage.reference
                val currentUser = Firebase.auth.currentUser
                val file = File(filePath)
                val audioRef = storageRef.child("audio/${currentUser?.uid}/dream_${System.currentTimeMillis()}.mp3")

                val uriFile = Uri.fromFile(file)

                // Attend que l'upload soit terminé
                val uploadTask = audioRef.putFile(uriFile).await()

                // Attend l'URL
                val uri = audioRef.downloadUrl.await()
                (dream.audio as MutableMap<String, Any>)["url"] = uri.toString()

                Log.d("AudioRecorder", "Fichier audio téléchargé avec succès à l'URL: $uri")
                Log.i("filePath", dream.audio.toString())
            } else {
                Log.i("filePath", "No audio file")
            }
            Log.d("dream5", dream.toString())

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

    fun addTag(tag: Tag, onSuccess: () -> Unit, onFailure: (Exception) -> Unit): Flow<DreamResponse> = flow {
        try {
            db.collection("tags")
                .add(tag)
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

    fun getCustomTag(userId: String): Flow<List<Tag>> = flow {
        try {
            val tags = db.collection("tags")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(Tag::class.java)?.copy(id = document.id)
                }

            Log.d("DreamRepository", "Tags retrieved successfully")
            Log.d("DreamRepository", tags.toString())

            emit(tags)

        } catch (e: Exception) {
            Log.e("DreamRepository", "Error retrieving tags", e)
            emit(emptyList())
        }
    }
}