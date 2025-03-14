package com.example.dreamary.views.activities.AllDreamsCalendar

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.viewmodels.AllDreamsCalendar.AllDreamsCalendarViewModel
import com.example.dreamary.viewmodels.profile.AllDreamsCalendarViewModelFactory
import com.example.dreamary.views.components.Loading
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.forEach
import kotlin.text.isNotEmpty

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
        factory = AllDreamsCalendarViewModelFactory (DreamRepository(LocalContext.current), (AuthRepository(LocalContext.current))
    ))
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
                    text = "Calendrier de rêves",
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
        LazyColumn {
            item {
                ResearchForAdream(dreams, navController)
            }

            item {
                FilterDreams(
                    categories = listOf(
                        CategoryDream("Tous", R.drawable.badge),
                        CategoryDream("Cauchemar", R.drawable.badge),
                        CategoryDream("Lucide", R.drawable.badge),
                        CategoryDream("Rêve", R.drawable.badge)
                    ),
                    selectedCategory = selectedCategory.value,
                    onCategorySelected = { selectedCategory.value = it }
                )
            }
            item {
                DreamCalendarScreen(dreams, userData, selectedCategory.value)
            }
            item {
                LegendColorDream()
            }
        }
    }
    }
}

fun onResearchChange(research: String, dreams: List<Dream>): List<Dream> {
    Log.d("Research", "Recherche de rêve: $research")
    Log.d("Research", "Liste des rêves: $dreams")
    var dreamsFound = emptyList<Dream>()
    if (research.isNotEmpty()) {
        dreams.forEach { dream ->
            if (dream.title.contains(research, ignoreCase = true)) {
                Log.d("Research", "Rêve trouvé: ${dream.title}")
                // !! il trouve mais n'ajoute pas correctement à dreamsFound
                dreamsFound.plus(dream)
            }
        }
    }
    Log.d("Research", "Résultat de la recherche: $dreamsFound")
    return dreamsFound
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchForAdream(
    dreams: List<Dream>,
    navController: NavController
) {
    var research by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var dreamsFound = emptyList<Dream>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recherchez un rêve",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(12.dp))
                .shadow(4.dp, RoundedCornerShape(12.dp))
        ) {
            OutlinedTextField(
                value = research,
                onValueChange = {
                    research = it
                    dreamsFound = onResearchChange(it, dreams)
                },
                placeholder = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recherchez un rêve",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // Important to link the menu to TextField
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Dropdown menu
            if (expanded) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    if (dreamsFound.isEmpty() && research.isNotEmpty()) {
                        DropdownMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            text = {
                                Text(
                                    text = "Aucun rêve trouvé",
                                    textAlign = TextAlign.Center,
                                )
                            },
                            onClick = { expanded = false }
                        )
                    } else {
                        dreamsFound.forEach { dream ->
                            DropdownMenuItem(
                                modifier = Modifier.fillMaxWidth(),
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = dream.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = dream.createdAt.toDate().toString(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    navController.navigate(
                                        NavRoutes.DreamDetail.createRoute(dream.id)
                                    )
                                }
                            )
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

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DreamCalendarScreen(
    dreams: List<Dream>,
    userData: User?,
    selectedCategory: String
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
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    Log.i("firstDayOfWeek", firstDayOfWeek.toString())

    val state = rememberCalendarState(
        startMonth = userAccountCreatedAccountDate?.yearMonth ?: currentMonth.minusMonths(1),
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
        outDateStyle = OutDateStyle.EndOfRow,
    )

    Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
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
                Log.i("daytodate", day.date.toString())
                Log.i("dreamFound", dreamsForToday.toString())

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Cauchemar" -> Color(0xFFffdbda)
                                dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Lucide" -> Color(0xFFefdefe)
                                dreamsForToday.isNotEmpty() && dreamsForToday[0].dreamType == "Reve" -> Color(0xFFdce6fc)
                                //isToday -> Color.Gray
                                isSelected -> Color.Blue
                                else -> Color.Transparent
                            }
                        )
                        .clickable { selectedDate.value = day.date },
                    contentAlignment = Alignment.Center
                ) {
                    Log.d("dateToday", day.date.toString())
                    Log.d("Dream", "Rêve trouvé: $dreamsForToday")
                    if (dreamsForToday.isNotEmpty()) {
                        if (dreamsForToday.size > 1) {
                            Text(
                                text = dreamsForToday.size.toString(),
                                color = Color.Red
                            )
                        }

                        // todo : afficher une modale avec le ou les rêves de la journée si on clique dessus
                        // todo : mettre des icones à la place du jour du mois
                        // todo : ajouter possibilité de filtrer par type de rêve
                        Log.d("Dream511", "Rêve trouvé: $dreamsForToday")
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (dreamsForToday[0].dreamType === "Cauchemar") Color(0xFFce5656) else if (dreamsForToday[0].dreamType === "Lucide") Color(0xFFa25ce6) else Color(0xFF5682d5)
                        )
                    } else {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
                .aspectRatio(1f)
        )
    }
}

