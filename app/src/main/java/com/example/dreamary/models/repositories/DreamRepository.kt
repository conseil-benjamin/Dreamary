package com.example.dreamary.models.repositories

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.dreamary.models.entities.Badge
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.states.DreamResponse
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.gson.Gson
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
import java.time.Instant
import java.time.ZoneId

class DreamRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val _userBadges = MutableStateFlow<List<Badge>>(emptyList())
    var userBadges = _userBadges.asStateFlow()

    private val _dream = MutableStateFlow<Dream?>(null)
    var dream = _dream.asStateFlow()

    suspend fun getUserBadgesViewModel(): StateFlow<List<Badge>> {
        try {
            Log.i("getUserBadges", "est rentré dans getUserBadges")
            val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userId = userFirebase.getString("uid", "").toString()
            Log.i("userId", userId.toString())

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

    /**
     * todo : essayer avec un utilisateur qui n'a pas de collections badge encore
     */

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
    fun updateProgressionBadges(
        allBadges: List<Badge>,
        badgesUser: List<Badge>,
        user: User,
        latestDreamTimestamp: Long
    ): Flow<List<Badge>> = flow {
        var updatedBadges = badgesUser.toMutableList()
        val newBadges = mutableListOf<Badge>()
        val sharedPreferences = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)

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

            // todo : marche pas n'ajoute pas les xp au profil de l'utilisateur

            if (unlocked && !existingBadge?.unlocked!!) {
                coroutineScope {
                    launch {
                        incrementUserXP(user, badge.xp)
                    }
                }
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
        emit(updatedBadges)
    }

    private fun incrementUserXP(user: User, badgeXp: Long){
        val sharedPreferences = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)
        val savedUser = sharedPreferences.getString("userDatabase", "")

        val gson = Gson()
        var userObject = gson.fromJson(savedUser, User::class.java)
        var level = (userObject.progression["level"] as? Number)?.toInt() ?: 0
        var xp = (userObject.progression["xp"] as? Number)?.toInt() ?: 0
        var xpNeeded = (userObject.progression["xpNeeded"] as? Number)?.toInt() ?: 0
        var rank = userObject.progression["rank"] as String

        Log.i("xpBadge", badgeXp.toString())
        xp += badgeXp.toInt()

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

        userObject = userObject.copy(
            progression = mapOf(
                "level" to level,
                "xp" to xp,
                "rank" to rank,
                "xpNeeded" to xpNeeded
            )
        )

        val editor = sharedPreferences.edit()
        val json = gson.toJson(userObject)
        editor.putString("userDatabase", json)
        editor.apply()

        val userMap = userToMap(userObject)
        db.collection("users")
            .document(user.uid)
            .update(userMap)
            .addOnSuccessListener {
                Log.d("DreamRepository", "User XP successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("DreamRepository", "Error updating user XP", e)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUser(dream: Dream): Flow<Map<String, Any>> = flow {
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
        var nbDreams = userObject.dreamStats["totalDreams"] as Int
        val nbLucidDream = if (dream.lucid) userObject.dreamStats["lucidDreams"] as Int + 1 else userObject.dreamStats["lucidDreams"] as Int
        Log.i("nbLucidDream", nbLucidDream.toString())
        val nbNightmares = if (dream.dreamType == "Cauchemar") userObject.dreamStats["nightmares"] as Int + 1 else userObject.dreamStats["nightmares"] as Int
        var xpGained = 0

        if (actualStreak >= 3) {
            xp += 100
            xpGained += 100
        } else {
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
        if ((cal1.get(Calendar.DAY_OF_MONTH) != cal2.get(Calendar.DAY_OF_MONTH))) {
            Log.i("cal", "oaduadad")
            currentStreak = userObject.dreamStats["currentStreak"] as Int + 1
        } else if ((cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) && nbDreams == 0) {
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

        // todo : remettre à jour les sharedpreferences avant de rediriger vers la page de succès
        // todo : comme ca on récupère les données à jour

        // todo : ajouter de l'xp à l'utilisateur pour chaque nouveau badge gagné

        val allBadges: List<Badge> = getAllBadges().toList().flatten()
        val badgesUser: List<Badge> = getUserBadges().toList().flatten()
        Log.i("badges", allBadges.toString())
        Log.i("badgesUser", badgesUser.toString())

        val updatedProgressionBadges = updateProgressionBadges(allBadges, badgesUser, userObject, dream.createdAt.seconds).toList().flatten()
        Log.i("updatedBadgesProgression", updatedProgressionBadges.toString())

        // todo : mettre à jour les badges de l'utilisateur si il en a débloquer de nouveau

        // todo : je parcours tout les badges et je vérifie le critère d'obtention s'il est validé je
        // todo : le garde de côté, ensuite une fois que j'ai la liste des badges qu'il possède je
        // todo : vérifie si l'utilisateur a déjà ou non ces badges et si non je les ajoute
        // todo : voir si on peut pas par exemple proposer plusieurs niveau pour le même badge
        // todo : genre un badge rare 7 jours d'affilé et un épique 30 jours d'affilé et donc remplacer le 7 jours par le 30 jours (à voir plus tard)

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
            dream.id = Timestamp.now().toString() + dream.userId

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
                        updateUser(dream)
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

    fun getDreams(): Flow<List<Dream>> = flow {
        try {
            val dreams = db.collection("dreams")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(Dream::class.java)?.copy(id = document.id)
                }
            emit(dreams)
        } catch (e: Exception) {
            Log.e("DreamRepository", "Error retrieving dreams", e)
            emit(emptyList())
        }
    }

    fun getDreamById(idDream: String): StateFlow<Dream?> {
        try {
            db.collection("dreams")
                .document(idDream)
                .get()
                .addOnSuccessListener { document ->
                    val dream = document.toObject(Dream::class.java)
                    _dream.value = dream
                    Log.d("DreamRepository", "Dream data retrieved: $dream")
                }
                .addOnFailureListener { e ->
                    Log.e("DreamRepository", "Error getting user data", e)
                }
        } catch (e: Exception) {
            Log.e("DreamRepository", "Error retrieving dream", e)
        }
        return dream
    }

}