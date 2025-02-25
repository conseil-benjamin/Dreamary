package com.example.dreamary.views.activities.AllDreamsCalendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.viewmodels.AllDreamsCalendar.AllDreamsCalendarViewModel
import com.example.dreamary.viewmodels.profile.AllDreamsCalendarViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllDreamsCalendar(
    navController: NavController,
    viewModel: AllDreamsCalendarViewModel = viewModel(
        factory = AllDreamsCalendarViewModelFactory ((DreamRepository(LocalContext.current))
    ))
) {

    val dreams by viewModel.dreams.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getDreams()
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.onSurface),
        topBar = {
            // TopNavigation(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
        LazyColumn {
            item {
                Calendar(dreams)
            }
        }
    }
    }
}

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Calendar(
    dreams: List<Dream>
) {
    val today = LocalDate.now()
    val context = LocalContext.current

    /**
     * 1. Récupérer le mois actuel et l'année actuelle
     * 2. récupérer le nombre de jours dans le mois
     * 3. récupérer le jour de la semaine du premier jour du mois
     * 4. On parcourt les jours du mois en vérifiant si on a un rêve pour chaque jour
     * 5. Si on a un rêve, on affiche le rêve dans le calendrier
     * 6. Si on a pas de rêve, on affiche rien
     * 7. Ensuite on passe au mois d'avant
     * 8. Mais avant ceci on vérifie si l'user avait un compte avant ce mois ci ou à ce mois ci
     * 9. Et si oui on continue d'afficher les rêves jusqu'à temps qu'on arrive au mois de création du compte du client
     */
}