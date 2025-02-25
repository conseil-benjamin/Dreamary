package com.example.dreamary.views.activities.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.viewmodels.home.HomeViewModel
import com.example.dreamary.viewmodels.home.HomeViewModelFactory
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.CustomSnackbarHost
import com.example.dreamary.views.components.TopNavigation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import com.example.dreamary.R
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.views.components.Loading
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import com.google.gson.Gson
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun PreviewHomeActivity() {
    HomeActivity(navController = NavController(LocalContext.current))
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("RestrictedApi")
@Composable
fun HomeActivity(navController: NavController, viewModel: HomeViewModel = viewModel(
    factory = HomeViewModelFactory(DreamRepository(LocalContext.current), AuthRepository(LocalContext.current))
)) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val dreams by viewModel.dreams.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val isLoading by viewModel.isLoading.collectAsState()
    val userState by viewModel.userData.collectAsState()
    val context = LocalContext.current

    // Modification du LaunchedEffect
    LaunchedEffect(Unit) {
        SnackbarManager.snackbarMessages.collect { snackbarMessage ->
            snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = snackbarMessage.actionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(userState) {
        Log.i("HomeActivity", "Sauvegarde de l'utilisateur")
        val gson = Gson()
        if (userState == null) {
            return@LaunchedEffect
        }

        val userJson = gson.toJson(userState)
        context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE).edit().putString("userDatabase", userJson).apply()
        Log.i("sharedPreferences", "Utilisateur sauvegardé en JSON : $userJson")
    }

    LaunchedEffect(Unit) {
        // todo faire en sorte de faire les requêtes uniquement si nécessaire
        // todo : donc par exemple si l'utilisateur a déjà chargé les rêves et qu'il en a pas ajouter de nouveau depuis on ne fait pas de requête
        // todo : pareil pour les infos de l'utilisateur si par exemple on déclare une variable preferences profileHasBeenUpdated et on vérifie si elle est à true
        // todo : si elle est à true ca veut dire que les infos en base de données on été changé et donc qu'on est plus à jour en local
        // todo : donc on fait une requête pour mettre à jour les informations en local

        Log.i("HomeActivity", "Récupération des rêves")
        Log.i("dreamssss", dreams.toString())
        viewModel.getTwoDreams(FirebaseAuth.getInstance().currentUser!!.uid, coroutineScope)
        viewModel.getProfileData(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    LaunchedEffect(dreams) {
        Log.i("dreamss", dreams.toString())

        if (dreams.isNotEmpty()) {
            if (!isUserHasAcurrentStreak(userState, dreams) && userState?.dreamStats?.get("currentStreak") != 0) {
                Log.i("HomeActivity", "L'utilisateur perd sa streak")
                viewModel.updateUserStats(user?.uid.toString(), "currentStreak", 0, coroutineScope)
            }
        } else {
            Log.i("HomeActivity", "Les rêves ne sont pas encore chargés.")
        }
    }

    DreamaryTheme {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onSurface),
            bottomBar = { BottomNavigation(navController = navController) },
            topBar = { TopNavigation(navController = navController) },
            snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            if (isLoading || userState == null) {
                Loading()
                return@Scaffold
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                LazyColumn (
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ){
                    item {
                        Stats(
                            userState =  userState
                        )
                    }
                    item{
                        LastTwoDreams(
                            dreams,
                            navController
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun Stats(
    userState: User?
) {
    val xp: Long = userState?.progression?.get("xp") as Long
    val xpNeeded: Long = userState.progression["xpNeeded"] as Long
    val progess = xp.toFloat() / xpNeeded.toFloat()

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ){
        Column (
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(16.dp),
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(
                        text = "Niveau ${userState?.progression?.get("level")}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userState.dreamStats["currentStreak"] != 0){
                        AsyncImage(
                            model ="https://cdn-icons-png.flaticon.com/512/785/785116.png",
                            contentDescription = "Streak icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${userState.dreamStats["currentStreak"]} jours de suite",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.fire),
                            contentDescription = "Streak icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Pas de suite",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
                LinearProgressIndicator(
                    progress = progess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "XP: ${userState?.progression?.get("xp")} / ${userState?.progression?.get("xpNeeded")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
}

fun isUserHasAcurrentStreak(user: User?, dreams: List<Dream>?): Boolean {
    Log.i("HomeActivity", "Vérification de la suite")
    Log.i("dreamsss", dreams.toString())
    if (user?.dreamStats?.get("currentStreak") == 0) {
        return false
    } else {
        Log.i("HomeActivity", "L'utilisateur a une suite")
        Log.i("HomeActivity", dreams?.isNotEmpty().toString())
        Log.i("HomeActivity", dreams?.get(0).toString())
        if (dreams?.size != 0) {
            Log.i("HomeActivity", "L'utilisateur a des rêves")
            Log.i("dreamList", dreams.toString())
            val lastDream = dreams?.firstOrNull()
            Log.i("lastdream", lastDream.toString())
            val timestamp = lastDream?.metadata?.get("createdAt") as Timestamp
            val date = timestamp.toDate()
            val cal1 = Calendar.getInstance().apply { time = date }
            val cal2 = Calendar.getInstance()
            if (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) - 1 || cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                Log.i("HomeActivity", "L'utilisateur a un rêve d'hier ou aujourd'hui")
                return true
            } else {
                Log.i("HomeActivity", "date incorrecte")
                return false
            }
        }
    }
    Log.i("HomeActivity", "L'utilisateur n'a pas de suiteeeeeee")
    return false
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LastTwoDreams(dreams: List<Dream>?, navController: NavController) {
    val haptic = LocalHapticFeedback.current

    if (dreams.isNullOrEmpty()) {
        Column (
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aucun rêve récent à afficher.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        return
    }
    Column (
        modifier = Modifier
            .padding(16.dp),
    ) {
        Row (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lune),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Dream icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Derniers rêves",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable {
                    navController.navigate(NavRoutes.AllDreamsCalendar.route)
                }
            ){
                Text(
                    text = "Voir tout",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(id = R.drawable.arrow),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Arrow right",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            }
        }
        dreams.forEach { dream ->
            val timestamp = dream.metadata["createdAt"] as Timestamp
            val date = timestamp.toDate()
            val localDate = timestamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val dateJourMoisAnnee = "${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"

            val cal1 = Calendar.getInstance().apply { time = date }
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(NavRoutes.DreamDetail.createRoute(dream.id))
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = dream.title,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (dream.lucid) {
                            Card (
                                modifier = Modifier
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onPrimary),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            ){
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ){
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Lucide",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(8.dp)
                                    )
                                    Text(
                                        text = "Lucide",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = dream.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            dream.emotions.take(2).forEach { emotion ->
                                    Card (
                                        modifier = Modifier
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onPrimary),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    ) {
                                        Text(
                                            text = emotion,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp),
                                        )
                                    }
                            }
                        }

                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = if (cal1.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance()
                                        .get(Calendar.DAY_OF_MONTH)
                                ) "Aujourd'hui" else if (cal1.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance()
                                        .get(Calendar.DAY_OF_MONTH) - 1
                                ) "Hier" else dateJourMoisAnnee,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

