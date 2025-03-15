package com.example.dreamary.views.activities.Social

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModel
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModelFactory
import com.example.dreamary.viewmodels.Social.LeaderboardViewmodel
import com.example.dreamary.viewmodels.Social.LeaderboardViewmodelFactory

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
    Text(
        text = "Salut !"
    )
}