package com.example.dreamary.views.activities.AllDreamsCalendar

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.viewmodels.AllDreamsCalendar.AllDreamsCalendarViewModel
import com.example.dreamary.viewmodels.profile.AllDreamsCalendarViewModelFactory
import com.example.dreamary.views.components.Divider
import com.example.dreamary.views.components.Loading
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.forEach
import kotlin.text.isNotEmpty
import androidx.compose.material3.MenuDefaults

data class CategoryDream(
    val name: String,
    val icon: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllDreamsCalendar(
    navController: NavController,
    viewModel: AllDreamsCalendarViewModel = viewModel(
        factory = AllDreamsCalendarViewModelFactory(DreamRepository(LocalContext.current), AuthRepository(LocalContext.current))
    )
) {
    val dreams by viewModel.dreams.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val userId = Firebase.auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()

    val selectedCategory = remember { mutableStateOf("Tous") }

    LaunchedEffect(Unit) {
        Log.d("AllDreamsCalendar", "Avant getAllDreamsForUser pour userId: $userId")
        viewModel.getAllDreamsForUser(userId, coroutineScope)
        Log.d("AllDreamsCalendar", "Après getAllDreamsForUser")
        viewModel.getProfileData(userId)
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onSurface),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .background(Color(0xFF1A1A1A)),
                title = { Text(
                    text = "Reveries",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 18.sp,
                ) },
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
        if (userData == null) {
            Loading()
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                item {
                    ResearchForAdream(dreams, navController)
                }

                item {
                    DreamCalendarScreen(dreams, userData, selectedCategory.value, navController)
                }
            }
        }
    }
}

@Composable
fun ModalSelectDream(
    day: String,
    dreams: List<Dream>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    navcontroller: NavController
) {
    Log.d("ModalSelectDream", dreams.toString())
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
        ) {
            // Bouton de fermeture en haut à droite
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = "Fermer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Contenu principal
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp, bottom = 16.dp)
                ) {
                    // En-tête
                    Text(
                        text = day,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    // Sous-titre ou message
                    Text(
                        text = "${dreams.size} ${if (dreams.size == 1 ) "rêve trouvé" else "rêves trouvés"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Liste des rêves
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dreams.size) {
                            CardDreamInModal(
                                dreams[it],
                                navcontroller
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDreamInModal(
    dream: Dream,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            navController.navigate(NavRoutes.DreamDetail.createRoute(dream.id))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête du rêve avec titre et type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Titre du rêve
                Text(
                    text = dream.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Type de rêve avec fond de couleur approprié
                val backgroundColor = when(dream.dreamType.lowercase()) {
                    "cauchemar" -> Color(0xFFffdbda)
                    "lucide" -> Color(0xFFefdefe)
                    "rêve" -> Color(0xFFdce6fc)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor = when(dream.dreamType.lowercase()) {
                    "cauchemar" -> Color(0xFFc62828)
                    "lucide" -> Color(0xFF6a1b9a)
                    "rêve" -> Color(0xFF1565c0)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = dream.dreamType,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu du rêve
            Text(
                text = dream.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Émotions en chips horizontales
            if (dream.emotions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Start
                ) {
                    dream.emotions.take(3).forEach { emotion ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = emotion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Indicateur pour plus d'émotions
                    if (dream.emotions.size > 3) {
                        Text(
                            text = "+${dream.emotions.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}


fun onResearchChange(research: String, dreams: List<Dream>): List<Dream> {
    Log.d("Research", "Recherche de rêve: $research")
    Log.d("Research", "Liste des rêves: $dreams")
    var dreamsFound = mutableListOf<Dream>()
    if (research.isNotEmpty()) {
        dreams.forEach { dream ->
            // todo : rechercher dans le titre, description, émotions, tags et dreamType
            // todo : les ordonner par date la plus récente
            // todo : peut être mettre une limite aussi à voir
            if (dream.title.contains(research, ignoreCase = true) || dream.content.contains(research, ignoreCase = true) || dream.dreamType.contains(research, ignoreCase = true)) {
                Log.d("Research", "Rêve trouvé: ${dream.title}")
                dreamsFound.add(dream)
            }
        }
    }
    Log.d("Research", "Résultat de la recherche: $dreamsFound")
    return dreamsFound
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchForAdream(
    dreams: List<Dream>,
    navController: NavController
) {
    var research by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var dreamsFound = remember { mutableListOf<Dream>() }

    Column(
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        // todo: faire en sorte que si l'on sort de la recherche et qu'on revient dessus cela remette
        // todo: à jour les dreamsFound
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .padding(0.dp)
        ) {
            OutlinedTextField(
                value = research,
                onValueChange = {
                    research = it
                    expanded = true
                    dreamsFound = onResearchChange(it, dreams) as MutableList<Dream>
                },
                placeholder = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recherchez un rêve...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Dropdown menu
            if (expanded && research.isNotEmpty()) {
                Log.i("dreamsFound45", dreamsFound.toString())
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .heightIn(max = 220.dp) // définit la taille maximale du menu déroulant
                ) {
                    if (dreamsFound.isEmpty() && research.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Aucun résultat",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucun rêve trouvé",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        dreamsFound.forEach { dream ->
                            val localDate = dream.createdAt.toDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            val dateJourMoisAnnee = "${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"

                            DropdownMenuItem(
                                modifier = Modifier
                                    .padding(8.dp),
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    leadingIconColor = MaterialTheme.colorScheme.primary,
                                    trailingIconColor = MaterialTheme.colorScheme.outline,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                ),
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .padding(vertical = 6.dp)
                                    ) {
                                        // En-tête avec titre et date
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = dream.title,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Text(
                                                text = dateJourMoisAnnee,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Type de rêve
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (dream.dreamType.contains("lucide", ignoreCase = true))
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.tertiaryContainer,
                                                    contentColor = if (dream.dreamType.contains("lucide", ignoreCase = true))
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onTertiaryContainer
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    if (dream.dreamType.contains("lucide", ignoreCase = true)) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Type de rêve",
                                                            modifier = Modifier.size(14.dp),
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Type de rêve",
                                                            modifier = Modifier.size(14.dp),
                                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = dream.dreamType,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Émotions
                                        if (dream.emotions.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 8.dp)
                                            ) {
                                                dream.emotions.take(2).forEach { emotion ->
                                                    Card(
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                        ),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                                    ) {
                                                        Text(
                                                            text = emotion,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        )
                                                    }
                                                }

                                                // Indicateur pour plus d'émotions
                                                if (dream.emotions.size > 2) {
                                                    Text(
                                                        text = "+${dream.emotions.size - 2}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    navController.navigate(
                                        NavRoutes.DreamDetail.createRoute(dream.id)
                                    )
                                },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.Default.Star,
//                                        contentDescription = "Rêve",
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(24.dp)
//                                    )
//                                },
//                                trailingIcon = {
//                                    Icon(
//                                        imageVector = Icons.Default.PlayArrow,
//                                        contentDescription = "Voir détails",
//                                        tint = MaterialTheme.colorScheme.outline,
//                                        modifier = Modifier.size(16.dp)
//                                    )
//                                }
                            )

                            // Séparateur entre les éléments
                            if (dream != dreamsFound.last()) {
                                Divider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDreams(
    categories: List<CategoryDream>,
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
                            painter = painterResource(id = category.icon),
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
fun LegendColorDream() {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFFffdbda)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cauchemar",
                color = Color(0xFFce5656),
                modifier = Modifier
                    .padding(10.dp, 4.dp, 10.dp, 4.dp),
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFFefdefe)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                color = Color(0xFFa25ce6),
                text = "Rêve lucide",
                modifier = Modifier
                    .padding(10.dp, 4.dp, 10.dp, 4.dp),
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFFdce6fc)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Rêve",
                modifier = Modifier
                    .padding(10.dp, 4.dp, 10.dp, 4.dp),
                color = Color(0xFF5682d5)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@SuppressLint("RememberReturnType", "UnusedBoxWithConstraintsScope")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DreamCalendarScreen(
    dreams: List<Dream>,
    userData: User?,
    selectedCategory: String,
    navController: NavController
) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    Log.d("User2", "Utilisateur récupéré: $userData")
    val userAccountCreatedAccountDate = userData?.metadata?.get("createdAt")?.let { it as? Timestamp }
        ?.toDate()
        ?.toInstant()
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()
    var showModalDreams = remember { mutableStateOf(false) }
    var dreamsToShowInModal = remember { mutableListOf<Dream>() }
    var dayDreamToShow = remember { mutableStateOf("") }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = userAccountCreatedAccountDate?.yearMonth ?: currentMonth.minusMonths(1),
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfRow,
    )
    Log.i("modalVisibility", showModalDreams.toString())
    if (showModalDreams.value) {
        ModalSelectDream(
            day = dayDreamToShow.value,
            dreams = dreamsToShowInModal,
            onConfirm = { showModalDreams.value = false },
            onDismiss = { showModalDreams.value = false },
            navcontroller = navController
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        Row (
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            DaysOfWeekTitle(
                daysOfWeek = daysOfWeek
            )
        }
        VerticalCalendar(
            state = state,
            monthHeader = { month ->
                Text(
                    text = month.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            },
            dayContent = { day ->
                val isSelected = day.date == selectedDate.value
                val isToday = day.date == today
                val isInCurrentMonth = day.position == DayPosition.MonthDate

                if (!isInCurrentMonth) {
                    Spacer(modifier = Modifier.size(40.dp))
                    return@VerticalCalendar
                }

                val dreamsForToday: List<Dream> = dreams.filter {
                    it.metadata["createdAt"]?.let { timestamp ->
                        (timestamp as? Timestamp)?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() == day.date
                    } == true
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Cauchemar" -> Color(0xFFffdbda)
                                    dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Lucide" -> Color(0xFFefdefe)
                                    dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Rêve" -> Color(0xFFdce6fc)
                                    isSelected -> Color.Blue
                                    else -> Color.Transparent
                                }
                            )
                            .clickable {
                                dreamsToShowInModal.clear()
                                dreamsToShowInModal.addAll(dreamsForToday)
                                dayDreamToShow.value = "${day.date.dayOfMonth}/${day.date.monthValue}/${day.date.year}"
                                showModalDreams.value = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Cauchemar" -> Color(0xFFce5656)
                                dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Lucide" -> Color(0xFFa25ce6)
                                dreamsForToday.isNotEmpty() -> Color(0xFF5682d5)
                                isSelected -> Color.White
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    if (dreamsForToday.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (5).dp, y = (-5).dp)
                                .size(16.dp)
                                .background(Color.Red, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (dreamsForToday.size > 9) "9+" else dreamsForToday.size.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .height(250.dp)
                .padding(8.dp)
        )
    }
}

