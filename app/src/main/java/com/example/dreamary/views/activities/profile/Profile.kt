package com.example.dreamary.views.activities.profile

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.dreamary.models.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.example.dreamary.viewmodels.profile.ProfileViewModel
import com.example.dreamary.R
import com.example.dreamary.models.entities.Badge
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.viewmodels.Profile.ProfileViewModelFactory
import com.example.dreamary.viewmodels.home.HomeViewModelFactory
import com.example.dreamary.views.components.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileActivity(
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory (AuthRepository(LocalContext.current), DreamRepository(LocalContext.current))
    ),
    navController: NavController
) {
    val user by viewModel.userData.collectAsState()
    val badges by viewModel.userBadges.collectAsState()
    Log.d("ProfileActivity", "User data: $user")

    val currentUser = FirebaseAuth.getInstance().currentUser
    val isVisitor = currentUser?.uid != user?.uid

    LaunchedEffect(Unit) {
        viewModel.getProfileData(currentUser?.uid ?: "")
        viewModel.getUserBadges()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
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
                Header(user = user, isVisitor = isVisitor)
            }

            item {
                Progression(
                    user = user
                )
            }

            item {
                DreamStatsSection(user = user)
            }

            // Section Badges
            item {
                BadgesSection(
                    user = user,
                    badges = badges,
                    navController = navController
                )
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
private fun Header(
    modifier: Modifier = Modifier,
    user: User?,
    isVisitor: Boolean
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

                if (isVisitor) {
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
                StatItem(value = user?.social?.get("followers").toString(), label = "Abonnés")
                StatItem(value = (user?.social?.get("groups") as List<*>).toString().length.toString(), label = "Groupes")
            }
        }
    }
}

@Composable
private fun Progression(
    user: User?
){
    val xp: Long = user?.progression?.get("xp") as Long
    val xpNeeded: Long = user?.progression?.get("xpNeeded") as Long
    val progess = xp.toFloat() / xpNeeded.toFloat()
    Log.d("Progression", "XP: $xp / $xpNeeded")

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Progression",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Niveau ${user?.progression?.get("level")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "${user?.progression?.get("rank")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LinearProgressIndicator(
            progress = progess,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "XP: ${user?.progression?.get("xp")} / ${user?.progression?.get("xpNeeded")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
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
                    icon = R.drawable.badge,
                    title = "Total des rêves",
                    value = user?.dreamStats?.get("totalDreams").toString(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.primary
                )

                StatCard(
                    icon = R.drawable.badge,
                    title = "Rêves lucides",
                    value = user?.dreamStats?.get("lucidDreams").toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.tertiary
                )

                StatCard(
                    icon = R.drawable.trophy,
                    title = "Meilleure série",
                    value = "${user?.dreamStats?.get("longestStreak")} jours",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.secondary
                )

                StatCard(
                    icon = R.drawable.badge,
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
    icon: Int,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgesSection(
    user: User?,
    badges : List<Badge>,
    navController: NavController
    ) {

    // todo : dans la liste des badges gagnés par l'utilisateur remplacé
    // todo : le nom du badge actuellement par un id d'un objet badge pour plus
    // todo : personnalisé l'affichage des badges avec des raretés, couleurs et images différentes
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
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Badges",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { navController.navigate(NavRoutes.AllBadges.route) }) {
                    Text("Voir tout")
                }
            }
            FlowRow (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                badges.take(3).forEach { badge ->
                    BadgeItem(
                        icon = badge.iconUrl,
                        name = badge.name,
                        rarity = badge.rarity,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    icon: String,
    name: String,
    rarity: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 8.dp),
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
                AsyncImage(
                    model = icon,
                    contentDescription = null,
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