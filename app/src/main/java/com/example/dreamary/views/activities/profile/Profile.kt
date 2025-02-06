package com.example.dreamary.views.activities.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamary.models.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.example.dreamary.viewmodels.profile.ProfileViewModel

@Preview
@Composable
fun ProfilePreview() {
    ProfileActivity(onNavigateBack = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileActivity(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileData = remember {
        ProfileData(
            name = "Marie Petit",
            level = 15,
            title = "Rêveuse Expérimentée",
            memberSince = "Jan 2024",
            dreamCount = 147,
            lucidDreams = 24,
            totalGroups = 3,
            isFriend = false
        )
    }
    val user by viewModel.userData.collectAsState()
    Log.d("ProfileActivity", "User data: $user")

    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        viewModel.getProfileData(currentUser?.uid ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
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
        if (user == null) {
            Loading()
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header avec dégradé
            item {
                Header(profileData = profileData, user = user)
            }

            item {
                DreamStatsSection(user = user)
            }

            // Section Badges
            item {
                BadgesSection()
            }

            // Section Succès
            item {
                AchievementsSection()
            }

            // Section Collections
            item {
                CollectionsSection()
            }
        }
    }
}

@Composable
private fun Loading(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Header(
    profileData: ProfileData,
    modifier: Modifier = Modifier,
    user: User?
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar et infos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.username?.take(2)?.uppercase() ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = user?.fullName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "@${user?.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Niveau ${user?.progression?.get("level")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = user?.progression?.get("rank").toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                if (!profileData.isFriend) {
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "Ajouter",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = user?.dreamStats?.get("totalDreams").toString(), label = "Rêves")
                StatItem(value = user?.dreamStats?.get("lucidDreams").toString(), label = "Lucides")
                StatItem(value = user?.social?.get("groups").toString(), label = "Groupes")
            }
        }
    }
}

@Composable
private fun DreamStatsSection(
    user: User?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Statistiques des rêves",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Grid(
                columns = 2,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    icon = Icons.Default.Star,
                    title = "Total des rêves",
                    value = user?.dreamStats?.get("totalDreams").toString(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.primary
                )

                StatCard(
                    icon = Icons.Default.Star,
                    title = "Rêves lucides",
                    value = user?.dreamStats?.get("lucidDreams").toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.tertiary
                )

                StatCard(
                    icon = Icons.Default.Star,
                    title = "Meilleure série",
                    value = "${user?.dreamStats?.get("bestStreak")} jours",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.secondary
                )

                StatCard(
                    icon = Icons.Default.Star,
                    title = "Série actuelle",
                    value = "${user?.dreamStats?.get("currentStreak")} jours",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun Grid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        // Calculer la largeur des éléments en tenant compte des contraintes minimales
        val itemWidth = (constraints.maxWidth / columns).coerceAtLeast(constraints.minWidth / columns)

        // Créer de nouvelles contraintes pour les éléments
        val itemConstraints = constraints.copy(
            minWidth = 0,  // Permettre une largeur minimale de 0
            maxWidth = itemWidth
        )

        // Mesurer les éléments
        val placeables = measurables.map { it.measure(itemConstraints) }

        // Calculer les dimensions de la grille
        val rows = (placeables.size + columns - 1) / columns
        val gridHeight = rows * (placeables.maxOf { it.height } + 16.dp.roundToPx())

        // Placer les éléments
        layout(constraints.maxWidth, gridHeight) {
            var y = 0
            var x = 0
            placeables.forEach { placeable ->
                placeable.place(x = x, y = y)
                x += itemWidth
                if (x >= constraints.maxWidth) {
                    x = 0
                    y += placeable.height + 16.dp.roundToPx()
                }
            }
        }
    }
}
@Composable
private fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun BadgesSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Badges",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BadgeItem(
                    icon = Icons.Default.Star,
                    name = "Première Lucidité",
                    rarity = "rare",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
                BadgeItem(
                    icon = Icons.Default.Star,
                    name = "30 Jours Consécutifs",
                    rarity = "légendaire",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
                BadgeItem(
                    icon = Icons.Default.Star,
                    name = "100 Rêves",
                    rarity = "épique",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun BadgeItem(
    icon: ImageVector,
    name: String,
    rarity: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = containerColor
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = rarity,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AchievementsSection() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Succès",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AchievementItem(
                name = "Explorer Onirique",
                progress = 75,
                total = 100
            )
            AchievementItem(
                name = "Maître de la Lucidité",
                progress = 24,
                total = 50
            )
            AchievementItem(
                name = "Collectionneur de Symboles",
                progress = 45,
                total = 50
            )
        }
    }
}

@Composable
private fun AchievementItem(
    name: String,
    progress: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$progress/$total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = progress.toFloat() / total,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun CollectionsSection() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Collections",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { /* TODO */ }) {
                Text("Voir tout")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class ProfileData(
    val name: String,
    val level: Int,
    val title: String,
    val memberSince: String,
    val dreamCount: Int,
    val lucidDreams: Int,
    val totalGroups: Int,
    val isFriend: Boolean
)