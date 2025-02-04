package com.example.dreamary.views.activities.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.dreamary.R
import com.example.dreamary.ui.theme.DreamaryTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.utils.SnackbarManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.text.get
import kotlin.toString

@Preview(showBackground = true)
@Composable
fun MoreInformationsPreview() {
    MoreInformations(navController = NavController(LocalContext.current))
}

fun createUser(email: String, fullName: String, username: String, bio: String, navController: NavController, context: Context, couroutineScope: CoroutineScope) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val user = User(
        uid = auth.currentUser?.uid ?: "",
        email = email,
        username = username,
        fullName = fullName,
        bio = bio,
        profilePictureUrl = "",
        metadata = mapOf(
            "accountStatus" to "active",
            "lastDreamDate" to Timestamp.now(),
            "isPremium" to false,
            "lastLogin" to Timestamp.now(),
            "createdAt" to Timestamp.now()
        ),
        preferences = mapOf(
            "notifications" to true,
            "theme" to "dark",
            "isPrivateProfile" to false,
            "language" to "fr"
        ),
        dreamStats = mapOf(
            "nightmares" to 0,
            "totalDreams" to 0,
            "lucidDreams" to 0,
            "longestStreak" to 0,
            "currentStreak" to 0
        ),
        achievements = mapOf(
            "unlockedBadges" to listOf<String>(),
            "totalBadges" to 0
        ),
        progression = mapOf(
            "xpNeeded" to 1000,
            "level" to 1,
            "xp" to 0,
            "rank" to "Débutant"
        ),
        social = mapOf(
            "groups" to listOf<String>(),
            "followers" to 0,
            "following" to 0
        )
    )

    if (bio.length > 100  || bio.isEmpty()) {
        couroutineScope.launch {
            SnackbarManager.showMessage(context.getString(R.string.MoreInformations_error_bio), R.drawable.error)
        }
        return
    } else if (username.length > 20 || username.isEmpty()) {
        couroutineScope.launch {
            SnackbarManager.showMessage(context.getString(R.string.MoreInformations_error_username), R.drawable.error)
        }
        return
    }

    db.collection("users").document(user.uid).set(user)
        .addOnSuccessListener {
            // Save user data in SharedPreferences
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("uid", user.uid)
                putString("email", user.email)
                putString("username", user.username)
                putString("fullName", user.fullName)
                putString("bio", user.bio)
                putString("profilePictureUrl", user.profilePictureUrl)
                putString("accountStatus", user.metadata["accountStatus"] as String)
                putString(
                    "lastDreamDate",
                    (user.metadata["lastDreamDate"] as Timestamp).toDate().toString()
                )
                putBoolean("isPremium", user.metadata["isPremium"] as Boolean)
                putString(
                    "lastLogin",
                    (user.metadata["lastLogin"] as Timestamp).toDate().toString()
                )
                putString(
                    "createdAt",
                    (user.metadata["createdAt"] as Timestamp).toDate().toString()
                )
                putBoolean("notifications", user.preferences["notifications"] as Boolean)
                putString("theme", user.preferences["theme"] as String)
                putBoolean("isPrivateProfile", user.preferences["isPrivateProfile"] as Boolean)
                putString("language", user.preferences["language"] as String)
                putInt("nightmares", user.dreamStats["nightmares"] as Int)
                putInt("totalDreams", user.dreamStats["totalDreams"] as Int)
                putInt("lucidDreams", user.dreamStats["lucidDreams"] as Int)
                putInt("longestStreak", user.dreamStats["longestStreak"] as Int)
                putInt("currentStreak", user.dreamStats["currentStreak"] as Int)
                putStringSet("unlockedBadges", (user.achievements["unlockedBadges"] as List<String>).toSet())
                putInt("totalBadges", user.achievements["totalBadges"] as Int)
                putInt("xpNeeded", user.progression["xpNeeded"] as Int)
                putInt("level", user.progression["level"] as Int)
                putInt("xp", user.progression["xp"] as Int)
                putString("rank", user.progression["rank"] as String)
                putStringSet("groups", (user.social["groups"] as List<String>).toSet())
                putInt("followers", user.social["followers"] as Int)
                putInt("following", user.social["following"] as Int)
                apply()
            }
            // Rediriger vers l'écran d'accueil
            val editor = context.getSharedPreferences("userInCreation", Context.MODE_PRIVATE).edit()
            editor.putBoolean("userInCreation", false)
            editor.apply()
            val editor2 = context.getSharedPreferences("isLoggedIn", Context.MODE_PRIVATE).edit()
            editor2.putBoolean("isLoggedIn", true)
            editor2.apply()
            couroutineScope.launch {
                SnackbarManager.showMessage(context.getString(R.string.Create_user_successfull), R.drawable.success)
            }
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(NavRoutes.UserMoreInformation.route) {
                    inclusive = true
                }
            }
        }
        .addOnFailureListener {
            couroutineScope.launch {
                SnackbarManager.showMessage(context.getString(R.string.Register_error_message), R.drawable.error)
            }
        }
}

@Composable
fun MoreInformations (navController: NavController) {
    /* Demander :
    - Pseudo (String) -> Vérifier que le pseudo n'est pas déjà pris
    - Bio (String)
    - Profilpic (Image) -> Stocker l'image dans Firebase Storage
     */
    var pseudo by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilPic by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isLoggedIn = context.getSharedPreferences("userInCreation", Context.MODE_PRIVATE)
    val editor = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    var email = editor.getString("email", "unknow email")
    var fullName = editor.getString("displayName", "unknow user")
    var photoUrl = editor.getString("photoUrl", "unknow photoUrl")


    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    // Ecoute des messages du SnackbarManager
    LaunchedEffect(Unit) { // unit veut dire que l'effet sera lancé une seule fois
        SnackbarManager.snackbarMessages.collect { snackbarMessage ->
            snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = snackbarMessage.actionLabel
            )
        }
    }


    DreamaryTheme {
        Scaffold (
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .paint(
                        painterResource(id = R.drawable.background),
                        contentScale = ContentScale.Crop,
                    )
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            }
            Column(
                modifier = Modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Plus que quelques informations pour finaliser votre inscription",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.MoreInformations_text_pseudo),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.MoreInformation_text_bio),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                )
                // récupérer image de profil

                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Enregistrer les informations dans la base de données
                            // on check si le pseudo n'est pas déjà pris
                            // on check si l'image est bien une image
                            // on check si la bio n'est pas trop longue
                            // Rediriger vers l'écran d'accueil
                            createUser(
                                email = email ?: "",
                                fullName = fullName ?: "",
                                username = pseudo,
                                bio = bio,
                                navController = navController,
                                context = context,
                                coroutineScope
                            )
                        }
                    }
                ) {
                    Text(
                        text = "Finaliser l'inscription",
                        color = Color.White
                    )
                }
            }
        }
    }
}