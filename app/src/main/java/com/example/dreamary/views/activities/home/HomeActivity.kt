package com.example.dreamary.views.activities.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar
import com.example.dreamary.R

@Preview(showBackground = true)
@Composable
private fun PreviewHomeActivity() {
    HomeActivity(navController = NavController(LocalContext.current))
}

@Composable
fun HomeActivity(navController: NavController, viewModel: HomeViewModel = viewModel(
    factory = HomeViewModelFactory(DreamRepository(LocalContext.current))
)) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val dreams = viewModel.dreams.value
    val user = FirebaseAuth.getInstance().currentUser

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

    LaunchedEffect(Unit) {
        Log.i("HomeActivity", "Récupération des rêves")
        viewModel.getTwoDreams(FirebaseAuth.getInstance().currentUser!!.uid, coroutineScope)
    }

    DreamaryTheme {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onSurface),
            bottomBar = { BottomNavigation(navController = navController) },
            topBar = { TopNavigation(navController = navController) },
            snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        ) { paddingValues ->
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
                            user
                        )
                    }
                    item{
                        LastTwoDreams(
                            dreams
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stats(
    user: FirebaseUser?
) {

}

@Composable
private fun LastTwoDreams(dreams: List<Dream>?){
    if (dreams.isNullOrEmpty()) {
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
                horizontalArrangement = Arrangement.Center
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
            val timestamp = dream.metadata["createdAt"] as Timestamp;
            val date = timestamp.toDate()
            val dateJourMoisAnnee = "${date.day}/${date.month}/${date.year}"

            val cal1 = Calendar.getInstance().apply { time = date }
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = dream.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (dream.isLucid) {
                            Text(
                                text = "Lucide",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Row {
                        Text(
                            text = dream.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
//                    dream.emotions.forEach( emotion ->
//                        Surface {
//                            Text(
//                                text = it,
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    )
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

