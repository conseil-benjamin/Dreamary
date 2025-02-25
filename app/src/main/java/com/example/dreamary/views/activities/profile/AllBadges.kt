package com.example.dreamary.views.activities.profile

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.viewmodels.profile.AllBadgesVIewModel
import com.example.dreamary.viewmodels.profile.AllBadgesViewModelFactory
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.Badge
import com.example.dreamary.views.components.Loading
import java.util.Locale

// Data classes
data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector
)

val categories = listOf(
    Category("all", "Tous", Icons.Default.AccountBox),
    Category("Régularité", "Régularité", Icons.Default.DateRange),
    Category("Volume", "Volume", Icons.Default.Star),
    Category("Exploration", "Exploration", Icons.Default.Star)
)

enum class Rarity(val color: Color, val textColor: Color) {
    COMMON(Color.Gray, Color.Gray),
    RARE(Color.Blue, Color.Blue),
    EPIC(Color.Magenta, Color.Magenta),
    LEGENDARY(Color.Yellow, Color.Yellow)
}

@Composable
fun HeaderPage(
    badges: List<Badge>,
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.badge),
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${badges.count { it.unlocked }} / ${badges.size}",
                fontSize = 18.sp
            )
        }
    }

}

@Composable
fun GridBadges(
    badges: List<Badge>,
    selectedCategory: String
) {
    val filteredBadges = badges.filter {
        selectedCategory == "all" || it.category == selectedCategory
    }

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
            items(filteredBadges) { badge ->
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
    val userBadges by viewModel.userBadges.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserBadges()
    }

    LaunchedEffect(userBadges) {
        Log.i("allBadges", userBadges.toString())
    }

    var selectedCategory by remember { mutableStateOf("all") }

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
        },
        bottomBar =  {
            RarityLegend()
        }
    ){ paddingValues ->
        if (userBadges.isEmpty()) {
            Loading()
            return@Scaffold
        }
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    HeaderPage(
                        badges = userBadges
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
                        badges = userBadges,
                        selectedCategory = selectedCategory
                    )
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
                selected = selectedCategory == category.name,
                onClick = { onCategorySelected(category.name) },
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
fun BadgeCard(badge: Badge) {
    val scale by animateFloatAsState(if (badge.color == "violet") 1f else 0.95f)

    val color = when (badge.rarity) {
        "Commun" -> Rarity.COMMON
        "Rare" -> Rarity.RARE
        "Epique" -> Rarity.EPIC
        "Legendaire" -> Rarity.LEGENDARY
        else -> Rarity.COMMON
    }

    Card(
        modifier = Modifier
            .scale(scale)
            .border(
                width = 2.dp,
                color = (if (badge.unlocked) color.color else Color.Gray.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { }
            .alpha(if (badge.unlocked) 1f else 0.5f),
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
                    .background(if (badge.unlocked) color.color else Color.Gray.copy(alpha = 0.2f)),
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
                fontSize = 16.sp,
                textAlign = TextAlign.Center
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
                progress = (badge.progression.toFloat() * 100) / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (badge.unlocked) color.color else Color.Gray
            )

            Text(
                text = "${((badge.progression.toFloat() / badge.objective.toFloat()) * 100).toInt()}% accompli",
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
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface),
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