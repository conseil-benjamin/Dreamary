import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.viewmodels.auth.MoreInformationViewModel
import com.example.dreamary.viewmodels.auth.MoreInformationViewModelFactory
import com.example.dreamary.views.components.DreamTextFieldCustom
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun MoreInformationsPreview() {
    MoreInformations(navController = NavController(LocalContext.current))
}

fun isUsernameAlreadyTaken(username: String, context: Context, callback: (Boolean) -> Unit): Boolean {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").whereEqualTo("username", username).get()
        .addOnSuccessListener { documents ->
            callback(documents.size() > 0)
        }
        .addOnFailureListener {
            callback(false)
        }
    return false
}

fun createUser(email: String, fullName: String, username: String, bio: String, profilePictureUri: String?, navController: NavController, context: Context, couroutineScope: CoroutineScope) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var tokenFcm = ""

    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        tokenFcm = token
        Log.i("token", token)
    }

    val user = User(
        uid = auth.currentUser?.uid ?: "",
        email = email,
        username = username,
        fullName = fullName,
        bio = bio,
        tokenFcm = tokenFcm,
        profilePictureUrl = profilePictureUri ?: "",
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
    } else if (profilePictureUri == null || profilePictureUri.isEmpty()) {
        couroutineScope.launch {
            SnackbarManager.showMessage(context.getString(R.string.MoreInformations_error_profile_picture), R.drawable.error)
        }
        return
    }

    isUsernameAlreadyTaken(username, context) { isTaken ->
        if (isTaken) {
            couroutineScope.launch {
                SnackbarManager.showMessage(context.getString(R.string.More_Informations_username_already_taken), R.drawable.error)
            }
        } else {
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
                        putString("tokenFcm", tokenFcm)
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
    }
}

@Composable
fun MoreInformations(
    navController: NavController,
    viewModel: MoreInformationViewModel = viewModel(
        factory = MoreInformationViewModelFactory(AuthRepository(LocalContext.current))
    )
) {
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePic by remember { mutableStateOf("") }

    val context = LocalContext.current
    val editor = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val email = editor.getString("email", "unknown email")
    val fullName = editor.getString("displayName", "unknown user")

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profilePic = uri.toString()
            viewModel.uploadProfilePicture(uri, context)
        }
    }

    val profilePictureUri by viewModel.profilePictureUri.collectAsState()

    // Listen for Snackbar messages
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarMessages.collect { snackbarMessage ->
            snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = snackbarMessage.actionLabel
            )
        }
    }

    DreamaryTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.background),
                        contentScale = ContentScale.Crop,
                    )
            ) {
                // Semi-transparent overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Créer votre profil",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Profile picture section
                    Box(
                        modifier = Modifier.padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePic.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fullName?.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        } else {
                            AsyncImage(
                                model = profilePic,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Camera button overlay
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                                .size(40.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.camera),
                                contentDescription = "Choose profile picture",
                                tint = Color.White
                            )
                        }
                    }

                    // Form fields with improved styling
                    DreamTextFieldCustom(
                        analysisText = username,
                        onTextChange = { username = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        label = "Nom d'utilisateur",
                        maxCharacters = 20,
                        maxLine = 1,
                        height = 56,
                        maxHeight = 56,
//                        leadingIcon = {
//                            Icon(
//                                painterResource(id = R.drawable.ic_person),
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
                    )

                    DreamTextFieldCustom(
                        analysisText = bio,
                        onTextChange = { bio = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        label = "Bio",
                        maxCharacters = 100,
                        maxLine = 3,
                        height = 96,
                        maxHeight = 120,
//                        leadingIcon = {
//                            Icon(
//                                painterResource(id = R.drawable.ic_description),
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
                    )

                    // Finalize button with gradient background
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                createUser(
                                    email = email ?: "",
                                    fullName = fullName ?: "",
                                    username = username,
                                    bio = bio,
                                    profilePictureUri = profilePictureUri,
                                    navController = navController,
                                    context = context,
                                    coroutineScope
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Finaliser",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Progress indicator
                    LinearProgressIndicator(
                        progress = when {
                            bio.length > 5 && username.length > 2 && profilePictureUri != null -> 1f    // 100% complete
                            bio.length > 5 && username.length > 2 -> 0.7f                               // 70% complete
                            bio.isNotEmpty() || username.isNotEmpty() -> 0.4f                           // 40% complete
                            else -> 0.1f                                                                // 10% complete - just started
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        text = when {
                            bio.length > 5 && username.length > 2 && profilePictureUri != null -> "100%"
                            bio.length > 5 && username.length > 2 -> "70%"
                            bio.isNotEmpty() || username.isNotEmpty() -> "40%"
                            else -> "10%"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}