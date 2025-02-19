package com.example.dreamary.views.activities.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.viewmodels.Profile.AllBadgesVIewModel
import com.example.dreamary.viewmodels.Profile.AllBadgesViewModelFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

// Data classes
data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class Badge(
    val id: Int,
    val name: String,
    val rarity: Rarity,
    val unlocked: Boolean,
    val progress: Int,
    val xp: Int
)

enum class Rarity(val color: Color, val textColor: Color) {
    COMMON(Color.Gray, Color.Gray),
    RARE(Color.Blue, Color.Blue),
    EPIC(Color.Magenta, Color.Magenta),
    LEGENDARY(Color.Yellow, Color.Yellow)
}

// todo : essayer pour le composant Rarity de le mettre en bottom du scaffold pour qu'il soit toujours visible

@Composable
fun HeaderPage(
    badges: Map<String, List<com.example.dreamary.views.activities.profile.Badge>>
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Collection de Badges",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        // Total Progress
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${
                    badges.values.flatten().count { it.unlocked }
                } / ${badges.values.flatten().size}",
                fontSize = 18.sp
            )
        }
    }

}

@Composable
fun GridBadges(
    badges: Map<String, List<Badge>>,
    selectedCategory: String
) {
    val filteredBadges = badges[selectedCategory] ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp) // ✅ Fixe une hauteur pour éviter le conflit avec LazyColumn
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(badges.entries.filter {
                selectedCategory == "all" || it.key == selectedCategory
            }.flatMap { it.value }) { badge ->
                BadgeCard(badge = badge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllBadges(
    navController : NavController,
    viewModel: AllBadgesVIewModel = viewModel(
        factory = AllBadgesViewModelFactory (DreamRepository(LocalContext.current))
    )
) {
    //val userBadges by viewModel.userBadges.collectAsState()
//
//    LaunchedEffect(Unit) {
//        viewModel.getUserBadges()
//    }

    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        Category("all", "Tous", Icons.Default.AccountBox),
        Category("regularity", "Régularité", Icons.Default.DateRange),
        Category("volume", "Volume", Icons.Default.Star),
        Category("exploration", "Exploration", Icons.Default.Star)
    )

    val badges = mapOf(
        "regularity" to listOf(
            Badge(1, "Premier Pas", Rarity.COMMON, true, 100, 50),
            Badge(2, "Rêveur Régulier", Rarity.RARE, true, 100, 100),
            Badge(3, "Maître du Journal", Rarity.EPIC, false, 60, 200),
            Badge(4, "Gardien des Songes", Rarity.LEGENDARY, false, 25, 500)
        ),
        "volume" to listOf(
            Badge(5, "Collectionneur", Rarity.COMMON, true, 100, 50),
            Badge(6, "Bibliothécaire", Rarity.RARE, false, 75, 150),
            Badge(7, "Chroniqueur", Rarity.EPIC, false, 45, 300)
        ),
        "exploration" to listOf(
            Badge(8, "Explorateur Lucide", Rarity.RARE, true, 100, 100),
            Badge(9, "Maître Lucide", Rarity.EPIC, false, 40, 250),
            Badge(10, "Tisseur d'Histoires", Rarity.LEGENDARY, false, 20, 500)
        )
    )

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Collections de badges") },
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
    ){ paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    HeaderPage(
                        badges = badges
                    )
                }

                item {
                    CategoryFilters(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }

                item {
                    GridBadges(
                        badges = badges,
                        selectedCategory = selectedCategory
                    )
                }

                item {
                    RarityLegend()
                }
            }
        }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilters(
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category.id,
                onClick = { onCategorySelected(category.id) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(category.name)
                    }
                },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun BadgeCard(badge: com.example.dreamary.views.activities.profile.Badge) {
    val scale by animateFloatAsState(if (badge.unlocked) 1f else 0.95f)

    Card(
        modifier = Modifier
            .scale(scale)
            .border(
                width = 2.dp,
                color = if (badge.unlocked) badge.rarity.color else Color.Gray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { }
            .alpha(if (badge.unlocked) 1f else 0.6f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (badge.unlocked) badge.rarity.color else Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badge Name
            Text(
                text = badge.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // XP Reward
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${badge.xp} XP", fontSize = 14.sp)
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = badge.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (badge.unlocked) badge.rarity.color else Color.Gray
            )

            Text(
                text = "${badge.progress}% accompli",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun RarityLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Rarity.entries.forEach { rarity ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(rarity.color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rarity.name.lowercase().capitalize(Locale.ROOT),
                    fontSize = 12.sp
                )
            }
        }
    }
}