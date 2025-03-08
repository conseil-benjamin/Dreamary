package com.example.dreamary.models.repositories

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.dreamary.models.entities.Badge
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.Share
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.states.DreamResponse
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class DreamRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val _userBadges = MutableStateFlow<List<Badge>>(emptyList())
    var userBadges = _userBadges.asStateFlow()

    private val _dream = MutableStateFlow<Dream?>(null)
    var dream = _dream.asStateFlow()

    suspend fun getUserBadgesViewModel(userId: String): StateFlow<List<Badge>> {
        try {
            Log.i("getUserBadges", "est rentré dans getUserBadges")

            val snapshot = db.collection("users")
                .document(userId)
                .collection("badges")
                .get()
                .await()

            Log.i("snapshot", snapshot.documents.toString())
            val badgeIds = snapshot.documents.mapNotNull { it.getString("badgeName") }
            Log.i("badgeIds", badgeIds.toString())

            if (badgeIds.isNotEmpty()) {
                // Récupérer les détails des badges correspondants
                val badgeSnapshot = db.collection("badges")
                    .whereIn("name", badgeIds)
                    .get()
                    .await()

                val badges = badgeSnapshot.documents.map { doc ->
                    Badge(
                        badgeId = doc.id,
                        category = doc.getString("category") ?: "",
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        iconUrl = doc.getString("iconUrl") ?: "",
                        rarity = doc.getString("rarity") ?: "",
                        color = doc.getString("color") ?: "#FFFFFF",
                        unlockCriteria = doc.get("unlockCriteria") as? Map<String, Any> ?: emptyMap(),
                        visibility = doc.getBoolean("visibility") ?: true,
                        progression = snapshot.documents.find { it.getString("badgeName") == doc.getString("name") }?.get("progression") as? Long ?: 0,
                        objective = doc.get("objective") as? Long ?: 0,
                        unlocked = snapshot.documents.find { it.getString("badgeName") == doc.getString("name") }?.getBoolean("unlocked") ?: false,
                        xp = doc.get("xp") as? Long ?: 0
                        )
                }
                Log.d("badgesUser", "Badges retrieved successfully: $badges")
                _userBadges.value = badges
            } else {
                Log.d("badgesUser", "No badges found for user")
                _userBadges.value = emptyList()
            }

        } catch (e: Exception) {
            Log.e("DreamRepository", "Error getting user badges", e)
            _userBadges.value = emptyList()
        }
        return userBadges
    }

    private fun getUserBadges(): Flow<List<Badge>> = flow {
        try {
            val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userId = userFirebase.getString("uid", "").toString()
            Log.i("userId", userId)

            // Récupérer les IDs des badges de l'utilisateur
            val snapshot = db.collection("users")
                .document(userId)
                .collection("badges")
                .get()
                .await() // ⚠️ Attendre le résultat
            Log.i("snapshotUser", snapshot.toString())

            val badgeIds = snapshot.documents.mapNotNull { it.getString("badgeName") }
            Log.i("badgeIdsUser", badgeIds.toString())

            if (badgeIds.isNotEmpty()) {
                // Récupérer les détails des badges correspondants
                val badgeSnapshot = db.collection("badges")
                    .whereIn("name", badgeIds)
                    .get()
                    .await()

                Log.i("badgeSnapshot", badgeSnapshot.documents.toString())

                val badges = badgeSnapshot.documents.map { doc ->
                    Badge(
                        badgeId = doc.id,
                        category = doc.getString("category") ?: "",
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        iconUrl = doc.getString("iconUrl") ?: "",
                        rarity = doc.getString("rarity") ?: "",
                        color = doc.getString("color") ?: "#FFFFFF",
                        unlockCriteria = doc.get("unlockCriteria") as? Map<String, Any> ?: emptyMap(),
                        visibility = doc.getBoolean("visibility") ?: true,
                        progression = snapshot.documents.find { it.getString("badgeName") == doc.getString("name") }?.get("progression") as? Long ?: 0,
                        unlocked = snapshot.documents.find { it.getString("badgeName") == doc.getString("name") }?.getBoolean("unlocked") ?: false,
                        objective = doc.get("objective") as? Long ?: 0,
                        xp = doc.get("xp") as? Long ?: 0
                    )
                }
                Log.d("badgesUser", "Badges retrieved successfully: $badges")
                emit(badges)
            } else {
                Log.d("badgesUser", "No badges found for user")
                emit(emptyList())
            }

        } catch (e: Exception) {
            Log.e("DreamRepository", "Error getting user badges", e)
            emit(emptyList())
        }
    }

    private fun getAllBadges(): Flow<List<Badge>> = flow {
        try {
            val snapshot = db.collection("badges").get().await()
            val badges = snapshot.documents.map {
                Badge(
                    badgeId = it.id,
                    name = it.getString("name") ?: "",
                    description = it.getString("description") ?: "",
                    iconUrl = it.getString("iconUrl") ?: "",
                    rarity = it.getString("rarity") ?: "",
                    color = it.getString("color") ?: "#FFFFFF",
                    category = it.getString("category") ?: "",
                    unlockCriteria = it.get("unlockCriteria") as? Map<String, Any> ?: emptyMap(),
                    xp = it.get("xp") as? Long ?: 0,
                )
            }
            Log.d("allbadges", "Badges retrieved successfully: $badges")
            emit(badges)
        } catch (e: Exception) {
            Log.e("DreamRepository", "Error getting badges", e)
            emit(emptyList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateProgressionBadges(
        allBadges: List<Badge>,
        badgesUser: List<Badge>,
        user: User,
        latestDreamTimestamp: Long
    ): User {
        var updatedBadges = badgesUser.toMutableList()
        val newBadges = mutableListOf<Badge>()
        val sharedPreferences = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)
        var userUpdated = user

        Log.i("userUpdated1", userUpdated.toString())
        Log.d("updateProgressionBadges", "Starting to process badges for user: ${user.uid}")
        Log.i("cacaAllBadges", "All badges: $allBadges")

        for (badge in allBadges) {
            if (!badge.visibility) {
                Log.d("updateProgressionBadges", "Badge ${badge.name} is not visible, skipping.")
                continue
            }

            Log.d("updateProgressionBadges", "Processing badge: ${badge.name}")

            val existingBadge = badgesUser.find { it.name == badge.name }
            Log.d("updateProgressionBadges", "Existing badge for ${badge.name}: $existingBadge")

            val newProgression = when (badge.unlockCriteria["type"]) {
                "dream_count" -> {
                    Log.d("updateProgressionBadges", "Badge ${badge.name} unlock criteria: dream_count")

                    // Vérification de la progression pour le badge "Première Plume"
                    if (badge?.name == "Première Plume" && (user.dreamStats["totalDreams"] ?: 0) > (existingBadge?.progression
                            ?: 0)) {
                        Log.d("updateProgressionBadges", "Progression increased based on totalDreams")
                        1
                    }
                    // Vérification de la progression pour le badge "Rêveur Assidu"
                    else if (badge?.name == "Rêveur Assidu" && (user.dreamStats["totalDreams"] ?: 0) > (existingBadge?.progression
                            ?: 0)) {
                        Log.d("updateProgressionBadges", "Progression increased based on totalDreams")
                        1
                    }
                    // Vérification de la progression pour les rêves lucides
                    else if (badge?.name == "Explorateur Lucide" && (user.dreamStats["lucidDreams"] ?: 0) > (existingBadge?.progression ?: 0)) {
                        Log.d("updateProgressionBadges", "Progression increased based on lucidDreams")
                        1
                    }
                    else {
                        Log.d("updateProgressionBadges", "No progress for ${existingBadge?.name}, no increase")
                        0
                    }
                }

                "time_range" -> { // Badge pour un rêve à une certaine heure
                    Log.d("updateProgressionBadges", "Badge ${badge.name} unlock criteria: time_range")

                    val startHour = (badge.unlockCriteria["startHour"] as? Long)?.toInt() ?: 0
                    val endHour = (badge.unlockCriteria["endHour"] as? Long)?.toInt() ?: 0
                    val dreamHour = Instant.ofEpochSecond(latestDreamTimestamp)
                        .atZone(ZoneId.systemDefault()).hour

                    Log.d("updateProgressionBadges", "Dream hour: $dreamHour, Range: $startHour - $endHour")

                    if (dreamHour in startHour until endHour) {
                        Log.d("updateProgressionBadges", "Progression increased based on time range")
                        1
                    } else {
                        Log.d("updateProgressionBadges", "No progress for ${badge.name} based on time range")
                        0
                    }
                }

                else -> {
                    Log.d("updateProgressionBadges", "No valid unlock criteria for ${badge.name}, no progression.")
                    0
                }
            }



            val goal = badge.unlockCriteria["count"] as? Long ?: 1
            val updatedProgression = (existingBadge?.progression ?: 0) + newProgression
            val unlocked = updatedProgression >= goal

            if (unlocked && existingBadge?.unlocked != true) {
                Log.d("salut:::::::", "Badge ${badge.name} unlocked")
                userUpdated = incrementUserXP(user, badge.xp)
            }

            Log.d("updateProgressionBadges", "Updated progression for ${badge.name}: $updatedProgression, Unlocked: $unlocked")

            if (existingBadge != null) {
                Log.d("cacaboudin", existingBadge.unlocked.toString())
                if (unlocked && existingBadge.unlocked) {
                    continue
                }
                Log.d("updateProgressionBadges", "Updating existing badge: ${badge.name}")
                updatedBadges = updatedBadges.map {
                    if (it.name == badge.name) it.copy(progression = updatedProgression, unlocked = unlocked) else it
                }.toMutableList()
            } else {
                if (newProgression == 0) {
                    Log.d("updateProgressionBadges", "No progress for ${badge.name}, skipping.")
                    continue
                }
                Log.d("updateProgressionBadges", "Adding new badge: ${badge.name}")
                val newBadge = badge.copy(progression = updatedProgression, unlocked = unlocked)
                newBadges.add(newBadge)
            }
        }

        updatedBadges.addAll(newBadges)

        Log.d("updateProgressionBadges", "Saving updated badges to shared preferences")
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(updatedBadges)
        editor.putString("badges", json)
        editor.apply()

        Log.d("updateProgressionBadges", "Saving updated badges to Firestore")
        val userId = user.uid
        val userRef = db.collection("users").document(userId).collection("badges")

        updatedBadges.forEach { badge ->
            val badgeRef = userRef.document(badge.badgeId)
            val goal = badge.unlockCriteria["count"] as? Long ?: 1

            badgeRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Le badge existe déjà → on met juste à jour sa progression
                    Log.d("updateProgressionBadges", "Badge ${badge.name} exists in Firestore, updating progression.")
                    badgeRef.set(
                        mapOf(
                            "progression" to badge.progression,
                            "unlocked" to badge.unlocked,
                            "updatedAt" to Timestamp.now()
                        ),
                        SetOptions.merge()
                    )
                } else {
                    // Le badge n'existe pas → on l'ajoute complètement
                    Log.d("updateProgressionBadges", "Badge ${badge.name} does not exist in Firestore, adding it.")
                    badgeRef.set(
                        mapOf(
                            "badgeName" to badge.name,
                            "progression" to badge.progression,
                            "unlocked" to badge.unlocked,
                            "createdAt" to Timestamp.now(),
                            "objective" to goal
                        )
                    )
                }
            }
        }

        Log.d("updateProgressionBadges", "Finished updating badges for user: ${user.uid}")
        return userUpdated
    }

    private suspend fun incrementUserXP(user: User, badgeXp: Long): User {
        val userRef = db.collection("users").document(user.uid)

        // Récupérer l'utilisateur depuis Firestore
        val snapshot = userRef.get().await()
        val userObject = snapshot.toObject(User::class.java) ?: return user

        // Extraire la progression actuelle
        var level = (userObject.progression["level"] as? Number)?.toInt() ?: 0
        var xp = (userObject.progression["xp"] as? Number)?.toInt() ?: 0
        var xpNeeded = (userObject.progression["xpNeeded"] as? Number)?.toInt() ?: 1000
        var rank = userObject.progression["rank"] as? String ?: ""

        // Ajouter l'XP du badge
        xp += badgeXp.toInt()
        Log.i("xpBadge", "Ajout de $badgeXp XP - Nouveau total: $xp / $xpNeeded")

        // Monter de niveau si nécessaire
        while (xp >= xpNeeded) {
            level++
            xp -= xpNeeded
            xpNeeded += 200
        }

        // Définir le rang selon le niveau
        rank = when {
            level >= 50 -> "Légende Onirique"
            level >= 30 -> "Grand Sage des Songes"
            level >= 20 -> "Maître des rêves"
            level >= 10 -> "Rêveur confirmé"
            else -> "Apprenti rêveur"
        }

        // Mettre à jour l'utilisateur avec les nouvelles valeurs
        val xpGained = user.progression["xpGained"] as? Number ?: (0 + badgeXp.toInt())
        val updatedUser = user.copy(
            progression = mapOf(
                "level" to level,
                "xp" to xp,
                "rank" to rank,
                "xpNeeded" to xpNeeded,
                "xpGained" to xpGained
            )
        )

        Log.i("updatedUserIncrement", "User updated: $updatedUser")

        return updatedUser
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUser(dream: Dream, type: String): Flow<Map<String, Any>> = flow {
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
        Log.i("xpActual", xp.toString())
        var xpNeeded = (userObject.progression["xpNeeded"] as? Number)?.toInt() ?: 0
        var rank = userObject.progression["rank"] as String
        val actualStreak = userObject.dreamStats["currentStreak"] as Int
        var longestStreak = userObject.dreamStats["longestStreak"] as Int
        var nbDreams = userObject.dreamStats["totalDreams"] as Int
        // todo : gérer le cas ou l'utilisateur change de type de rêve et donc on devra décrémenter le nombre de cauchemars si l'ancien est un cauchemar
        val nbLucidDream = if (dream.lucid) userObject.dreamStats["lucidDreams"] as Int + 1 else userObject.dreamStats["lucidDreams"] as Int
        val nbNightmares = if (dream.dreamType == "Cauchemar") userObject.dreamStats["nightmares"] as Int + 1 else userObject.dreamStats["nightmares"] as Int
        var xpGained = 0

        if (actualStreak >= 3 && type == "add") {
            xp += 100
            xpGained += 100
        } else if (actualStreak < 3 && type == "add") {
            xp += 50
            xpGained += 50
        }

        if (xp >= xpNeeded) {
            level += 1
            xp -= xpNeeded
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
        if ((cal1.get(Calendar.DAY_OF_MONTH) != cal2.get(Calendar.DAY_OF_MONTH)) && type == "add") {
            Log.i("cal", "oaduadad")
            currentStreak = userObject.dreamStats["currentStreak"] as Int + 1
        } else if ((cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) && nbDreams == 0 && type == "add") {
            Log.i("cal", "oaduadad")
            currentStreak = userObject.dreamStats["currentStreak"] as Int +1
        } else {
            Log.i("cal", "nannnnnnnnnnnnnnnnnnn")
            Log.i("cal", nbDreams.toString())
            currentStreak = userObject.dreamStats["currentStreak"] as Int
        }

        if (currentStreak >= longestStreak) {
            longestStreak = currentStreak
        }

        Log.i("userObjectBeforeUpdate", userObject.toString())
        Log.d("DEBUG", "Metadata: ${userObject.metadata}")

        // permet de convertir le createdAt en Timestamp si ce n'est pas déjà le cas
        // pour éviter les erreurs de type lors de la mise à jour
        val createdAtTimestamp = when (val createdAtValue = userObject.metadata["createdAt"]) {
            is Timestamp -> createdAtValue // Déjà un Timestamp, OK
            is Map<*, *> -> { // Convertir depuis un objet Map
                val seconds = (createdAtValue["seconds"] as? Number)?.toLong() ?: 0L
                val nanoseconds = (createdAtValue["nanoseconds"] as? Number)?.toInt() ?: 0
                Timestamp(seconds, nanoseconds)
            }
            else -> Timestamp.now()
        }

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
                "createdAt" to createdAtTimestamp
            ),
            preferences = mapOf(
                "notifications" to (userObject.preferences["notifications"] as? Boolean ?: true),
                "theme" to (userObject.preferences["theme"] as? String ?: "dark"),
                "isPrivateProfile" to (userObject.preferences["isPrivateProfile"] as? Boolean ?: false),
                "language" to (userObject.preferences["language"] as? String ?: "fr")
            ),
            dreamStats = mapOf(
                "nightmares" to nbNightmares,
                "totalDreams" to (userObject.dreamStats["totalDreams"] ?: 0) + 1,
                "lucidDreams" to nbLucidDream,
                "longestStreak" to longestStreak,
                "currentStreak" to currentStreak
            ),
            progression = mapOf(
                "level" to level,
                "xp" to xp,
                "rank" to rank,
                "xpNeeded" to xpNeeded,
                "xpGained" to xpGained
            ),
            social = mapOf(
                "followers" to (userObject.social["followers"] ?: 0),
                "following" to (userObject.social["following"] ?: 0),
            )
        )

        Log.i("userObject", userObject.toString())

        val allBadges: List<Badge> = getAllBadges().toList().flatten()
        val badgesUser: List<Badge> = getUserBadges().toList().flatten()
        Log.i("badges", allBadges.toString())
        Log.i("badgesUser", badgesUser.toString())

        Log.i("userBeforeBadgeIncrement", userObject.toString())
        val userUpdatedAfterBadgeIncrementXp = updateProgressionBadges(allBadges, badgesUser, userObject, dream.createdAt.seconds)
        Log.i("updatedBadgesProgression", userUpdatedAfterBadgeIncrementXp.toString())

        val editor = sharedPreferences.edit()
        val json = gson.toJson(userUpdatedAfterBadgeIncrementXp)
        editor.putString("userDatabase", json)
        editor.apply()
        Log.i("userUpdated", "Utilisateur mis à jour : $userObject")
        val userMap = userToMap(userUpdatedAfterBadgeIncrementXp as User)
        emit(userMap)
    }

    fun userToMap(user: User): Map<String, Any> {
        Log.i("userToMap", user.toString())
        return mapOf(
            "uid" to user.uid,
            "dreamStats" to user.dreamStats,
            "progression" to user.progression,
            "metadata" to user.metadata
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateDream(
        dream: Dream,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ): Flow<DreamResponse> = flow {
        val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        Log.i("dream5", dream.toString())

        try {
            if (dream.audio["path"] != "" && dream.audio["url"] == "") {
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
                Log.i("filePath", "No audio file or already uploaded")
            }
            Log.d("dream5", dream.toString())

            db.collection("users")
                .document(dream.userId)
                .collection("dreams")
                .document(dream.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        db.collection("users")
                            .document(dream.userId)
                            .collection("dreams")
                            .document(dream.id)
                            .set(dream)
                            .addOnSuccessListener {
                                Log.d("DreamRepository", "DocumentSnapshot successfully written!")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.w("DreamRepository", "Error writing document", e)
                                onFailure(e)
                            }
                    } else {
                        Log.d("DreamRepository", "Document does not exist, cannot update")
                        onFailure(Exception("Document does not exist, cannot update"))
                    }
                }

            Log.i("sharedWithUpdate", dream.sharedWith.toString())
            shareDreamWithUsers(dream, "update")

            coroutineScope {
                Log.d("DreamRepository", "Updating user")
                try {
                    updateUser(dream, "update")
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

    private suspend fun shareDreamWithUsers(dream: Dream, typeOfShare: String) {
        Log.d("DreamRepository", "Sharing dream with users")
        Log.i("dream5", dream.toString())
        Log.d("DreamRepository", "Dream shared with: ${dream.sharedWith.users}")

        var newUserToSendDream: Share = Share()
        var sharedWithInBdd: Share = Share()
        if (typeOfShare === "update") {
            val snapshot = db.collection("users")
                .document(dream.userId)
                .collection("dreams")
                .document(dream.id)
                .get()
                .await()

              sharedWithInBdd = snapshot.toObject(Dream::class.java)?.sharedWith!!
            for (user in dream.sharedWith.users) {
                Log.i("sharedWithUpdate", user.toString())
                if (!sharedWithInBdd.users.contains(user)) {
                    Log.i("sharedWithUpdate", "add")
                    newUserToSendDream.users = newUserToSendDream.users + user
                }
            }
        }
        Log.i("sharedWithInBdd", sharedWithInBdd.toString())
        Log.i("newUserSendDream", newUserToSendDream.toString())

            val shareUserList = if (typeOfShare === "update") newUserToSendDream else dream.sharedWith
            Log.i("sharedWithUpdateListFinale", shareUserList.toString())

            for (user in shareUserList.users) {
            Log.d("DreamRepository", "Sharing dream with user ${user.uid}")
            val uuid = UUID.randomUUID().toString()

            val message = mapOf(
                "id" to uuid,
                "content" to "Nouveau rêve partagé !",
                "createdAt" to Timestamp.now(),
                "senderId" to dream.userId,
                "dreamId" to dream.id,
                "receiverId" to user.uid,
                "seen" to false,
                "type" to "dream",
                "dream" to dream
            )

            val querySnapshot = db.collection("chats")
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("userId1", dream.userId),
                            Filter.equalTo("userId2", user.uid)
                        ),
                        Filter.and(
                            Filter.equalTo("userId1", user.uid),
                            Filter.equalTo("userId2", dream.userId)
                        )
                    )
                )
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                Log.d("DreamRepository", "Conversation already exists")

                querySnapshot.documents.first().reference.collection("messages")
                    .add(message)
                    .await()

                Log.d("DreamRepository", "Dream shared with user ${user.uid}")

                db.collection("chats")
                    .document(querySnapshot.documents.first().id)
                    .update(
                        mapOf(
                            "lastSender" to dream.userId,
                            "lastMessageTimestamp" to Timestamp.now(),
                            "lastMessage" to "Nouveau rêve partagé !",
                            "unreadMessagesUser1" to 0,
                            "unreadMessagesUser2" to 1,
                        )
                    )
                    .await()

                Log.d("DreamRepository", "Conversation updated")
            } else {
                Log.d("DreamRepository", "Creating new conversation")

                val userDream = db.collection("users").document(dream.userId).get().await()
                val uuid = UUID.randomUUID().toString()

                val conversationRef = db.collection("chats").document(uuid)
                conversationRef.set(
                    mapOf(
                        "userId1" to dream.userId,
                        "userId2" to user.uid,
                        "users" to listOf(dream.userId, user.uid),
                        "createdAt" to Timestamp.now(),
                        "chatId" to uuid,
                        "user1" to user,
                        "user2" to userDream.toObject(User::class.java),
                        "lastSender" to dream.userId,
                        "lastMessageTimestamp" to Timestamp.now(),
                        "lastMessage" to "Nouveau rêve partagé !",
                        "unreadMessageUser1" to 0,
                        "unreadMessageUser2" to 1,
                    )
                ).await()

                conversationRef.collection("messages")
                    .add(message)
                    .await()

                Log.d("DreamRepository", "New conversation created and dream shared with user ${user.uid}")
            }
        }
        if (typeOfShare === "update"){
            Log.i("sharedWithUpdateListFinale", newUserToSendDream.toString())
            Log.i("sharedWithUpdateListFinale", sharedWithInBdd.toString())
            val userListToUpdate: Share = Share(sharedWithInBdd.users + newUserToSendDream.users, emptyList())
            Log.i("sharedWithUpdateListFinale", userListToUpdate.toString())
            db.collection("users")
                .document(dream.userId)
                .collection("dreams")
                .document(dream.id)
                .update("sharedWith", userListToUpdate)
                .await()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addDream(
        dream: Dream,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
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
            dream.id = UUID.randomUUID().toString()

            db.collection("users")
                .document(dream.userId)
                .collection("dreams")
                .document(dream.id)
                .set(dream)
                .addOnSuccessListener {
                    Log.d("DreamRepository", "DocumentSnapshot successfully written!")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w("DreamRepository", "Error writing document", e)
                    onFailure(e)
                }

            Log.d("DreamRepository", dream.sharedWith.users.toString())
            shareDreamWithUsers(dream, "add")

            coroutineScope {
                Log.d("DreamRepository", "Updating user")
                    try {
                        updateUser(dream, "add")
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
            val dreams = db.collection("users")
                .document(userId)
                .collection("dreams")
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

    fun getDreamById(idDream: String, userId: String): StateFlow<Dream?> {
        try {
            Log.i("dreamId", idDream)
            db.collection("users")
                .document(userId)
                .collection("dreams")
                .whereEqualTo("id", idDream)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val dream = document.toObject(Dream::class.java)
                        _dream.value = dream
                    }
                }
                .addOnFailureListener() { e ->
                    Log.e("DreamRepository", "Error retrieving dream", e)
                }
        } catch (e: Exception) {
            Log.e("DreamRepository", "Error retrieving dream", e)
        }
        return dream
    }

}