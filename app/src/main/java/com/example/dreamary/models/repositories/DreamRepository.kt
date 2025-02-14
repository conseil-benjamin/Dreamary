package com.example.dreamary.models.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.states.DreamResponse
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.math.log

class DreamRepository(private val context: Context) {
    private val db = Firebase.firestore

    fun updateUser(): Flow<User> = flow {
        val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val savedUser = sharedPreferences.getString("user", null)  // 'null' si aucune valeur n'est trouvée
        Log.i("HomeActivity", "Utilisateur récupéré : $savedUser")
        val gson = Gson()
        var userObject = gson.fromJson(savedUser, User::class.java)
        var level = userObject.progression["level"] as Int
        var xp = userObject.progression["xp"] as Int
        var xpNeeded = userObject.progression["xpNeeded"] as Int
        var rank = userObject.progression["rank"] as String
        val hasAstreak = userObject.dreamStats["currentStreak"] as Int
        var longestStreak = userObject.dreamStats["longestStreak"] as Int

        if (hasAstreak >= 3) {
            xp += 100
        } else {
            xp += 50
        }

        if (xp >= xpNeeded) {
            level += 1
            xpNeeded += 200
        }

        if (level == 2) {
            rank = "Apprenti rêveur"
        } else if (level == 3) {
            rank = "Rêveur confirmé"
        } else if (level == 4) {
            rank = "Rêveur expert"
        } else if (level == 5) {
            rank = "Rêveur légendaire"
        }

        if (hasAstreak >= longestStreak) {
            longestStreak = hasAstreak
        }

        // todo faire les badges aussi
        // todo : pourquoi pas faire un écran quand on vient d'ajouter un rêve
        // todo : qui montrerait qu'il s'est bien ajouté déjà
        // todo : et ensuite qui montre nos stats donc notre currentStreak si ont en a une
        // todo : et qui montre notre avancement dans le niveau
        // todo : donc rediriger vers cette écran et non vers le Home et envoyer les données user
        // todo : mis à jour

        userObject = userObject.copy(
            dreamStats = mapOf(
                "nightmares" to (userObject.dreamStats["nightmares"] ?: 0),
                "totalDreams" to (userObject.dreamStats["totalDreams"] ?: 0) + 1,
                "lucidDreams" to (userObject.dreamStats["lucidDreams"] ?: 0),
                "longestStreak" to longestStreak,
                "currentStreak" to (userObject.dreamStats["currentStreak"] ?: 0) + 1
            ),
            progression = mapOf(
                "level" to level,
                "xp" to (userObject.progression["xp"] as? Int ?: 0),
                "rank" to (userObject.progression["rank"] as? String ?: "Unranked"),
                "xpNeeded" to (userObject.progression["xpNeeded"] as? Int ?: 100)
            ),
            metadata = mapOf(
                "accountStatus" to (userObject.metadata["accountStatus"] as? String ?: "active"),
                "lastDreamDate" to Timestamp.now(),
                "isPremium" to (userObject.metadata["isPremium"] as? Boolean ?: false),
                "lastLogin" to (userObject.metadata["lastLogin"] as? Timestamp ?: Timestamp.now()),
                "createdAt" to (userObject.metadata["createdAt"] as? Timestamp ?: Timestamp.now()
                        )
            )
        )

        // todo : remettre à jour les sharedpreferences avant de rediriger vers la page de succès
        // todo : comme ca on récupère les données à jour

        val editor = sharedPreferences.edit()
        val json = gson.toJson(userObject)
        editor.putString("user", json)
        editor.apply()
        Log.i("userUpdated", "Utilisateur mis à jour : $userObject")
        emit(gson.fromJson(json, Map::class.java) as Map<String, Any>)
    }

    fun addDream(
        dream: Dream,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        navController: NavController
    ): Flow<DreamResponse> = flow {
        try {
            if (dream.audio["path"] != "") {
                Log.i("filePath", dream.audio.toString())
                val filePath = dream.audio["path"] as String
                var storage = Firebase.storage
                val storageRef = storage.reference
                val currentUser = Firebase.auth.currentUser
                val file = File(filePath)
                val audioRef =
                    storageRef.child("audio/${currentUser?.uid}/dream_${System.currentTimeMillis()}.mp3")

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
                    updateUser().collect { updatedUser ->
                        db.collection("users")
                            .document(updatedUser.uid)
                            .update(updatedUser)
                            .addOnSuccessListener {
                                Log.d("DreamRepository", "DocumentSnapshot successfully updated!")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.w("DreamRepository", "Error updating document", e)
                            }
                        // Une fois la mise à jour réussie, on appelle onSuccess() pour rediriger
                    }


                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                    onFailure(e)
                }
            // TODO : Start implementing the logic to update the user's stats
        } catch (
            e: Exception
        ) {
            emit(DreamResponse.Error(e.message ?: "Une erreur est survenue"))
        }
    }

    fun addTag(
        tag: Tag,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ): Flow<DreamResponse> = flow {
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

    fun getDreamsForCurrentUser(
        userId: String,
        onFailure: (Exception) -> Unit
    ): Flow<List<Dream>> = flow {
        try {
            val dreams = db.collection("dreams")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(2)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(Dream::class.java)?.copy(id = document.id)
                }

            Log.d("DreamRepository", "Dreams retrieved successfully")
            Log.d("DreamRepository", dreams.toString())

            emit(dreams)

        } catch (e: Exception) {
            Log.e("DreamRepository", "Error retrieving dreams", e)
            onFailure(e)
            emit(emptyList())
        }
    }
}