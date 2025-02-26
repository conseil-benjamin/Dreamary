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
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
                        name = user?.displayName ?: "Unknown",
                        onClick = { navController.navigate(NavRoutes.Profile.createRoute(user?.uid ?: "")) }
                    )
                }
            }

            // Navigation
            item {
                SettingsSection(
                    title = "Navigation",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Journal des rêves",
                        subtitle = "Tous vos rêves",
                        onClick = { navController.navigate(NavRoutes.Home.route) }
                    )
                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Guide onirique",
                        subtitle = "Apprenez le rêve lucide",
                        onClick = { navController.navigate(NavRoutes.Home.route) }
                    )
                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Paramètres",
                        subtitle = "Personnalisez votre expérience",
                        onClick = { navController.navigate(NavRoutes.Settings.route) }
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
                        icon = Icons.Default.Star,
                        title = "Premium",
                        subtitle = "Accédez à toutes les fonctionnalités",
                        onClick = { navController.navigate(NavRoutes.Home.route) }
                    )
                    SettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        onClick = { navController.navigate(NavRoutes.Home.route) }
                    )
                    SettingItem(
                        icon = Icons.Default.Lock,
                        title = "Confidentialité",
                        onClick = { navController.navigate(NavRoutes.Home.route) }
                    )
                }
            }

            // Support
            item {
                SettingsSection(
                    title = "Support",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Aide & Support",
                        onClick = { navController.navigate(NavRoutes.Home.route) }
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
                    text = "Version 1.0.0",
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