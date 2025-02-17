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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.Calendar
import java.util.Date
import kotlin.math.log

class DreamRepository(private val context: Context) {
    private val db = Firebase.firestore

    fun updateUser(): Flow<Map<String, Any>> = flow {
        Log.i("UpdateUser", "est rentré dans updateUser")
        val sharedPreferences = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)
        val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        Log.i("sharedPreferences", sharedPreferences.toString())
        Log.i("sharedPreferences", sharedPreferences.all.toString())
        val savedUser = sharedPreferences.getString("userDatabase", "")
        Log.i("HomeActivity", "Utilisateur récupéré : $savedUser")
        val gson = Gson()
        var userObject = gson.fromJson(savedUser, User::class.java)
        var level = (userObject.progression["level"] as? Number)?.toInt() ?: 0
        var xp = (userObject.progression["xp"] as? Number)?.toInt() ?: 0
        var xpNeeded = (userObject.progression["xpNeeded"] as? Number)?.toInt() ?: 0
        var rank = userObject.progression["rank"] as String
        val actualStreak = userObject.dreamStats["currentStreak"] as Int
        var longestStreak = userObject.dreamStats["longestStreak"] as Int

        if (actualStreak >= 3) {
            xp += 100
        } else {
            xp += 50
        }

        if (xp >= xpNeeded) {
            level += 1
            xpNeeded += 200
        }

        // Mise à jour du rang
        rank = when (level) {
            1 -> "Apprenti rêveur"
            10 -> "Rêveur confirmé"
            20 -> "Maître des rêves"
            30 -> "Grand Sage des Songes"
            50 -> "Légende Onirique"
            else -> rank
        }

        val lastDreamDateMap = userObject.metadata["lastDreamDate"] as? Map<*, *>
        val lastDreamDate = lastDreamDateMap?.let {
            val seconds = (it["seconds"] as? Number)?.toLong() ?: 0L
            Date(seconds * 1000) // Convertir les secondes en millisecondes
        } ?: Date() // Valeur par défaut en cas d'erreur

        val cal1 = Calendar.getInstance().apply { time = lastDreamDate }
        val cal2 = Calendar.getInstance()
        var currentStreak: Int = 0
        Log.i("cal", cal1.toString())
        Log.i("cal", cal2.toString())
        if ((cal1.get(Calendar.DAY_OF_MONTH) != cal2.get(Calendar.DAY_OF_MONTH))) {
            Log.i("cal", "oaduadad")
            currentStreak = userObject.dreamStats["currentStreak"] as Int + 1
        } else {
            Log.i("cal", "dqzdqzpmdqzmdqz")
            currentStreak = userObject.dreamStats["currentStreak"] as Int
        }

        if (actualStreak >= longestStreak) {
            longestStreak = actualStreak
        }
        val listBadges = userObject.achievements["unlockedBadges"] as MutableList<String>

        for (i in listBadges) {
            if (currentStreak >= 30 && !listBadges.contains("30 jours consécutifs")) {
                listBadges.add("30 jours consécutifs")
            } else if (currentStreak >= 60 && !listBadges.contains("60 jours consécutifs")) {
                listBadges.add("60 jours consécutifs")
            } else if (currentStreak >= 90 && !listBadges.contains("90 jours consécutifs")) {
                listBadges.add("90 jours consécutifs")
            }
        }

        Log.i("userObject", userObject.toString())
        userObject = userObject.copy(
            uid = userFirebase.getString("uid", "").toString(),
            email = userObject.email,
            username = userObject.username,
            fullName = userObject.fullName,
            bio = userObject.bio,
            profilePictureUrl = userObject.profilePictureUrl,
            metadata = mapOf(
                "accountStatus" to (userObject.metadata["accountStatus"] as? String ?: "active"),
                "lastDreamDate" to Timestamp.now(),
                "isPremium" to (userObject.metadata["isPremium"] as? Boolean ?: false),
                "lastLogin" to (userObject.metadata["lastLogin"] as? Timestamp ?: Timestamp.now()),
                "createdAt" to (userObject.metadata["createdAt"] as? Timestamp ?: Timestamp.now()
                        )
            ),
            preferences = mapOf(
                "notifications" to (userObject.preferences["notifications"] as? Boolean ?: true),
                "theme" to (userObject.preferences["theme"] as? String ?: "dark"),
                "isPrivateProfile" to (userObject.preferences["isPrivateProfile"] as? Boolean ?: false),
                "language" to (userObject.preferences["language"] as? String ?: "fr")
            ),
            dreamStats = mapOf(
                "nightmares" to (userObject.dreamStats["nightmares"] ?: 0),
                "totalDreams" to (userObject.dreamStats["totalDreams"] ?: 0) + 1,
                "lucidDreams" to (userObject.dreamStats["lucidDreams"] ?: 0),
                "longestStreak" to longestStreak,
                "currentStreak" to currentStreak
            ),
            progression = mapOf(
                "level" to level,
                "xp" to xp,
                "rank" to rank,
                "xpNeeded" to xpNeeded
            ),
            achievements = mapOf(
                "totalBadges" to (userObject.achievements["totalBadges"] ?: 0),
                "unlockedBadges" to (userObject.achievements["unlockedBadges"] ?: listOf<String>())
            ),
            social = mapOf(
                "followers" to (userObject.social["followers"] ?: 0),
                "following" to (userObject.social["following"] ?: 0),
            )
        )

        Log.i("userObject", userObject.toString())

        // todo : remettre à jour les sharedpreferences avant de rediriger vers la page de succès
        // todo : comme ca on récupère les données à jour

        val editor = sharedPreferences.edit()
        val json = gson.toJson(userObject)
        editor.putString("userDatabase", json)
        editor.apply()
        Log.i("userUpdated", "Utilisateur mis à jour : $userObject")
        val userMap = userToMap(userObject)
        emit(userMap)
    }

    fun userToMap(user: User): Map<String, Any> {
        return mapOf(
            "uid" to user.uid,
            "dreamStats" to user.dreamStats,
            "progression" to user.progression,
            "metadata" to user.metadata
        )
    }

    fun addDream(
        dream: Dream,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        navController: NavController
    ): Flow<DreamResponse> = flow {
        val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)

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
                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                    onFailure(e)
                }

            coroutineScope {
                Log.d("DreamRepository", "Updating user")
                    try {
                        updateUser()
                            .collect { updatedUser ->
                                Log.i("DreamRepository", updatedUser.toString())
                                Log.d("DreamRepository", "Processing updated user")
                                Log.d("DreamRepository", updatedUser.get("uid").toString())
                                Log.d("firebase", userFirebase.getString("uid", "").toString())
                                db.collection("users")
                                    .document(userFirebase.getString("uid", "").toString())
                                    .update(updatedUser)
                                    .addOnSuccessListener {
                                        Log.d("DreamRepository", "DocumentSnapshot successfully updated!")
                                        onSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("DreamRepository", "Error updating document", e)
                                    }
                            }
                    } catch (e: Exception) {
                        Log.e("DreamRepository", "Error in updateUser process", e)
                    }
            }
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