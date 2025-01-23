package com.example.dreamary.views.activities.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.utils.SnackbarManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    db.collection("users").document(user.uid).set(user)
        .addOnSuccessListener {
            // Rediriger vers l'écran d'accueil
            val editor = context.getSharedPreferences("userInCreation", Context.MODE_PRIVATE).edit()
            editor.putBoolean("userInCreation", false)
            editor.apply()
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

    DreamaryTheme {
        Column (
            modifier = Modifier
                .paint(
                painterResource(id = R.drawable.background),
                contentScale = ContentScale.Crop
            )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        }
        Text(
            text = "Plus que quelques informations pour finaliser votre inscription",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
        Column (
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = pseudo,
                onValueChange = {pseudo = it},
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_field_password),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            )
            OutlinedTextField(
                value = bio,
                onValueChange = {bio = it},
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_field_password),
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
                Text(text = "Finaliser l'inscription")
            }
        }
    }
}