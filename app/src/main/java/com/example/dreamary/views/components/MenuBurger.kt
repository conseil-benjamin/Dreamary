import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson

@Preview
@Composable
fun MenuBurgerScreenPreview() {
    DreamaryTheme { // Assurez-vous d'utiliser votre thème personnalisé
        CompositionLocalProvider(
            LocalContext provides LocalContext.current
        ) {
            MenuBurgerScreen(
                onNavigateBack = {},
                navController = NavController(LocalContext.current)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBurgerScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
) {
    val auth = Firebase.auth
    val context = LocalContext.current
    val user = auth.currentUser

    val userJson = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)
        .getString("userDatabase", null)

    var profilPicture: String? = null

    if (userJson != null) {
        val gson = Gson()
        val userState = gson.fromJson(userJson, User::class.java) // Remplacez `User` par votre classe utilisateur
        profilPicture = userState.profilePictureUrl // Assurez-vous que cette propriété existe dans votre classe `User`
        Log.i("userDatabase", profilPicture.toString())
    } else {
        Log.i("userDatabase", "Aucun utilisateur trouvé dans le cache.")
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Menu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compte
            item {
                SettingsSection(
                    title = "Compte",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    UserInfoCard(
                        name = user?.displayName ?: "Nom d'utilisateur",
                        profilePicture = profilPicture ?: "Unknown",
                        onClick = { navController.navigate(NavRoutes.Profile.createRoute(user?.uid ?: "")) }
                    )
                }
            }

            // Navigation
            item {
                SettingsSection(
                    title = "Navigation",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    SettingItem(
                        icon = R.drawable.book2,
                        title = "Journal des rêves",
                        subtitle = "Tous vos rêves",
                        onClick = { navController.navigate(NavRoutes.AllDreamsCalendar.route) }
                    )
                    SettingItem(
                        icon = R.drawable.guide,
                        title = "Guide onirique",
                        subtitle = "Apprenez le rêve lucide",
                        onClick = { navController.navigate(NavRoutes.Guide.route) }
                    )
                }
            }

            // Préférences
            item {
                SettingsSection(
                    title = "Préférences",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingItem(
                        icon = R.drawable.premium,
                        title = "Premium",
                        subtitle = "Accédez à toutes les fonctionnalités",
                        onClick = { navController.navigate(NavRoutes.Premium.route) }
                    )
                    SettingItem(
                        icon = R.drawable.settings,
                        title = "Paramètres",
                        subtitle = "Personnalisez votre expérience",
                        onClick = { navController.navigate(NavRoutes.Settings.route) }
                    )
//                    SettingItem(
//                        icon = R.drawable.notification,
//                        title = "Notifications",
//                        onClick = { navController.navigate(NavRoutes.Home.route) }
//                    )
//                    SettingItem(
//                        icon = R.drawable.lock,
//                        title = "Confidentialité",
//                        onClick = { navController.navigate(NavRoutes.Home.route) }
//                    )
                }
            }

            // Support
            item {
                SettingsSection(
                    title = "Support",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingItem(
                        icon = R.drawable.info,
                        title = "Aide & Support",
                        onClick = { navController.navigate(NavRoutes.Support.route) }
                    )
                }
            }

            // Déconnexion
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        auth.signOut()
                        context.getSharedPreferences("isLoggedIn", 0).edit()
                            .putBoolean("isLoggedIn", false).apply()
                        navController.navigate(NavRoutes.Login.route)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sign_out),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Déconnexion",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Version
            item {
                Text(
                    text = "Version 0.1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}