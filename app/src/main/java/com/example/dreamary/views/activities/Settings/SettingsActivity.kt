import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSection: (String) -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Paramètres") },
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
                AccountSection(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Préférences
            item {
                PreferencesSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onNavigateToSection = onNavigateToSection
                )
            }

            // Groupes
            item {
                GroupsSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onNavigateToSection = onNavigateToSection
                )
            }

            // Support
            item {
                SupportSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onNavigateToSection = onNavigateToSection
                )
            }

            // Déconnexion
            item {
                LogoutButton(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Version
            item {
                VersionInfo(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountSection(modifier: Modifier = Modifier) {
    val user = FirebaseAuth.getInstance().currentUser
    SettingsSection(
        title = "Compte",
        modifier = modifier
    ) {
        UserInfoCard(
            name = user?.displayName ?: "Unknown",
            email = user?.email ?: "Unknown",
            onClick = {}
        )
    }
}

@Composable
private fun PreferencesSection(
    modifier: Modifier = Modifier,
    onNavigateToSection: (String) -> Unit
) {
    SettingsSection(
        title = "Préférences",
        modifier = modifier
    ) {
        SettingItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            onClick = { onNavigateToSection("notifications") }
        )
        SettingItem(
            icon = Icons.Default.Face,
            title = "Apparence",
            onClick = { onNavigateToSection("appearance") }
        )
        SettingItem(
            icon = Icons.Default.Lock,
            title = "Confidentialité",
            onClick = { onNavigateToSection("privacy") }
        )
    }
}

@Composable
private fun GroupsSection(
    modifier: Modifier = Modifier,
    onNavigateToSection: (String) -> Unit
) {
    SettingsSection(
        title = "Groupes",
        modifier = modifier
    ) {
        SettingItem(
            icon = Icons.Default.Face,
            title = "Gestion des groupes",
            subtitle = "3 groupes actifs",
            onClick = { onNavigateToSection("groups") }
        )
    }
}

@Composable
private fun SupportSection(
    modifier: Modifier = Modifier,
    onNavigateToSection: (String) -> Unit
) {
    SettingsSection(
        title = "Support",
        modifier = modifier
    ) {
        SettingItem(
            icon = Icons.Default.Info,
            title = "Aide & Support",
            onClick = { onNavigateToSection("help") }
        )
    }
}

@Composable
private fun LogoutButton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Déconnexion",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun VersionInfo(modifier: Modifier = Modifier) {
    Text(
        text = "Version 1.0.0",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
    )
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit // correspond au type void en java ou Unit en kotlin
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
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

@Composable
private fun UserInfoCard(
    name: String,
    email: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = name.take(2).uppercase(),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name)
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}