import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.ui.theme.DreamaryTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.dreamary.R

@Composable
fun MenuBurger(navController: NavController) {
    val auth = Firebase.auth
    val context = LocalContext.current

    val user = auth.currentUser
    if (user == null) {
        navController.navigate("login") {
            popUpTo("home") { inclusive = true }
            launchSingleTop = true
        }
    }

    val userLevel = context.getSharedPreferences("user", 0).getInt("level", 1)
    val rank = context.getSharedPreferences("user", 0).getString("rank", "Débutant(e)")

    DreamaryTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.lune_selected),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = user?.displayName ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Niveau $userLevel - $rank",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Settings Section
            MenuSection(
                title = stringResource(id = R.string.Burger_text_compte),
                items = listOf(
                    MenuItem(
                        icon = { Icon(painterResource(id = R.drawable.settings), null) },
                        title = stringResource(id = R.string.Burger_Settings),
                        subtitle = stringResource(id = R.string.Burger_account_manage),
                        onClick = { navController.navigate("settings") }
                    ),
                    MenuItem(
                        icon = { Icon(painterResource(id = R.drawable.premium), null) },
                        title = stringResource(id = R.string.Burger_Premium),
                        subtitle = stringResource(id = R.string.Burger_got_all_features),
                        onClick = { /* TODO: Navigate to premium */ }
                    )
                )
            )

            // Navigation Section
            MenuSection(
                title = "Navigation",
                items = listOf(
                    MenuItem(
                        icon = { Icon(painterResource(id = R.drawable.lune), null) },
                        title = stringResource(id = R.string.Burger_Dream_diary),
                        subtitle = stringResource(id = R.string.Burger_all_your_dreams),
                        onClick = { /* TODO: Navigate to dream diary */ }
                    ),
                    MenuItem(
                        icon = { Icon(painterResource(id = R.drawable.book2), null) },
                        title = stringResource(id = R.string.Burger_onirique),
                        subtitle = stringResource(id = R.string.Burger_dream_lucid),
                        onClick = { /* TODO: Navigate to guide */ }
                    )
                )
            )

            // Support Section
            MenuSection(
                title = "Support",
                items = listOf(
                    MenuItem(
                        icon = { Icon(painterResource(id = R.drawable.help), null) },
                        title = stringResource(id = R.string.Burger_help_and_support),
                        onClick = { /* TODO: Navigate to help */ }
                    )
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    auth.signOut()
                    context.getSharedPreferences("isLoggedIn", 0).edit()
                        .putBoolean("isLoggedIn", false).apply()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(id = R.string.Burger_btn_logout))
            }
        }
    }
}

@Composable
private fun MenuSection(
    title: String,
    items: List<MenuItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    MenuItemRow(item)
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItem) {
    Surface(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                    item.icon()
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title)
                if (item.subtitle != null) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class MenuItem(
    val icon: @Composable () -> Unit,
    val title: String,
    val subtitle: String? = null,
    val onClick: () -> Unit
)