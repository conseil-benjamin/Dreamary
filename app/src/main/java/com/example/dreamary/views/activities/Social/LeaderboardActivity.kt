package com.example.dreamary.views.activities.Social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModel
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModelFactory
import com.example.dreamary.viewmodels.Social.LeaderboardViewmodel
import com.example.dreamary.viewmodels.Social.LeaderboardViewmodelFactory
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.TopNavigation

@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewmodel = viewModel(
        factory = LeaderboardViewmodelFactory (
            socialRepository = SocialRepository(
                LocalContext.current
            )
        )
    )
) {

    DreamaryTheme {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onSurface),
            bottomBar = { BottomNavigation(navController = navController) },
            topBar = { TopNavigation(navController = navController) }
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddingValues)
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onSurface,
                    text = "À venir !"
                )
            }
        }
    }
}