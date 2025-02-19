package com.example.dreamary.models.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.dreamary.models.entities.Badge
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.asStateFlow

class DreamRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val _userBadges = MutableStateFlow<List<Badge>>(emptyList())
    var userBadges = _userBadges.asStateFlow()

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
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        iconUrl = doc.getString("iconUrl") ?: "",
                        rarity = doc.getString("rarity") ?: "",
                        color = doc.getString("color") ?: "#FFFFFF",
                        unlockCriteria = doc.get("unlockCriteria") as? Map<String, Any> ?: emptyMap()
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

    fun getUserBadges(): Flow<List<Badge>> = flow {
        try {
            val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userId = userFirebase.getString("uid", "").toString()

            // Récupérer les IDs des badges de l'utilisateur
            val snapshot = db.collection("users")
                .document(userId)
                .collection("badges")
                .get()
                .await() // ⚠️ Attendre le résultat

            val badgeIds = snapshot.documents.mapNotNull { it.getString("badgeId") }

            if (badgeIds.isNotEmpty()) {
                // Récupérer les détails des badges correspondants
                val badgeSnapshot = db.collection("badges")
                    .whereIn("badgeId", badgeIds)
                    .get()
                    .await() // ⚠️ Attendre le résultat

                val badges = badgeSnapshot.documents.map { doc ->
                    Badge(
                        badgeId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        iconUrl = doc.getString("iconUrl") ?: "",
                        rarity = doc.getString("rarity") ?: "",
                        color = doc.getString("color") ?: "#FFFFFF",
                        unlockCriteria = doc.get("unlockCriteria") as? Map<String, Any> ?: emptyMap()
                    )
                }
                Log.d("badgesUser", "Badges retrieved successfully: $badges")
                emit(badges)
            } else {
                emit(emptyList())
            }

        } catch (e: Exception) {
            Log.e("DreamRepository", "Error getting user badges", e)
            emit(emptyList())
        }
    }

    fun getAllBadges(): Flow<List<Badge>> = flow {
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
                    unlockCriteria = it.get("unlockCriteria") as? Map<String, Any> ?: emptyMap()
                )
            }
            Log.d("allbadges", "Badges retrieved successfully: $badges")
            emit(badges)
        } catch (e: Exception) {
            Log.e("DreamRepository", "Error getting badges", e)
            emit(emptyList())
        }
    }

    fun checkIfUserHaveNewBadge(badges: List<Badge>, user: User): Flow<List<Badge>> = flow {
        Log.i("badges", user.toString())
        var listBadges: List<Badge> = emptyList()
        Log.i("badgeAtttennnd", user.dreamStats["totalDreams"].toString())
        for (i in badges.indices) {
            val dreamsAdded = badges[i].unlockCriteria["dreamsAdded"] as? Int ?: 0
            val totalDreamsAdded = user.dreamStats["totalDreams"] as? Int ?: 0
            val criteriaLucid = badges[i].unlockCriteria["lucidDreams"] as? Int ?: 0

            if ((dreamsAdded == totalDreamsAdded) || (dreamsAdded <= totalDreamsAdded)) {
                Log.i("badgeOuaissss", badges[i].unlockCriteria["dreamsAdded"].toString())
                listBadges += badges[i]
            } else if (criteriaLucid == user.dreamStats["lucidDreams"]){
                listBadges += badges[i]
            }
            else {
                Log.i("badgeOuaissss", badges[i].unlockCriteria["dreamsAdded"].toString())
                Log.i("badgeNonnn", badges[i].toString())
                Log.i("badgeNonnn", (badges[i].unlockCriteria["dreamsAdded"] == user.dreamStats["totalDreams"]).toString())
            }
        }
        emit(listBadges)
    }

    fun updateUserBadges(badges: List<Badge>, badgesUser: List<Badge>): Flow<List<Badge>> = flow {
        Log.i("badges", badges.toString())
        val sharedPreferences = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)
        val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)

        val badgesUpdated = badgesUser.toMutableList()
        val newBadges = mutableListOf<Map<String, Any>>()

        for (badge in badges) {
            if (badge !in badgesUser) {
                badgesUpdated.add(badge)
                newBadges.add(
                    mapOf(
                        "badgeName" to badge.name,
                        "createdAt" to Timestamp.now()
                    )
                )
            }
        }
        Log.i("badgesUpdated", badgesUpdated.toString())


        val editor = sharedPreferences.edit()
        val json = Gson().toJson(badgesUpdated)
        editor.putString("badges", json)
        editor.apply()

        val userId = userFirebase.getString("uid", "").toString()
        val userRef = db.collection("users").document(userId).collection("badges")

        newBadges.forEach { badgeData ->
            userRef.add(badgeData)
        }

        emit(badgesUpdated)
    }

    fun updateProgressionBadges(allBadges: List<Badge>, badgesUser: List<Badge>){
        // todo : avoir également des badges qui ont une visibilité à false pour plus de fun
        // todo : faire en sorte d'avoir un attribut unlocked et non unlocked pour les badges
        // todo : le but est de mettre à jour les badges de l'utilisateur en fonction de sa progression
        // todo:  par exemple on prend le premier badge de la liste de tous les badges et on vérifie à
        // todo : à quel point l'utilisateur est avancé
        // todo : par exemple avec le badge "Enregistrer 5 cauchemars" on va vérifier

        // todo : donc la on doit remettre à jour les badges user qu'on à auparavant déjà mis à jour
        // todo : avec cette fois ci les données lié à la progression

        for (i in allBadges.indices){
            val badge = allBadges[i]
            if (badge.visibility == true){
                // todo : on met à jour la progression

            }

            val badgeUser = badgesUser[i]
            val progression = badge.progression
            val progressionUser = badgeUser.progression

            if (progressionUser != progression){
                // todo : on met à jour les badges de l'utilisateur
            }
        }
    }


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
        val nbLucidDream = if (dream.lucid == true) userObject.dreamStats["lucidDreams"] as Int + 1 else userObject.dreamStats["lucidDreams"] as Int
        Log.i("nbLucidDream", nbLucidDream.toString())

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
                "lucidDreams" to nbLucidDream,
                "longestStreak" to longestStreak,
                "currentStreak" to currentStreak
            ),
            progression = mapOf(
                "level" to level,
                "xp" to xp,
                "rank" to rank,
                "xpNeeded" to xpNeeded
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

        val newBadges: List<Badge> = checkIfUserHaveNewBadge(allBadges, userObject).toList().flatten()
        Log.i("newBadges", newBadges.toString())

        val updatedBadges: List<Badge> = updateUserBadges(newBadges, badgesUser).toList().flatten()
        Log.i("updatedBadges", updatedBadges.toString())

        val updatedProgressionBadges = updateProgressionBadges(allBadges, badgesUser)
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
}