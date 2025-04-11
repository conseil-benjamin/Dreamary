package com.example.dreamary.views.activities.Guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.CustomSnackbarHost
import com.example.dreamary.views.components.TopNavigation

@Composable
fun GuideActivity(
    navController: NavController
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
                   .background(MaterialTheme.colorScheme.surface)
                   .fillMaxSize()
                   .padding(paddingValues)
           ) {
               Text(
                   textAlign = TextAlign.Center,
                   text = "A venir ...",
                   color = MaterialTheme.colorScheme.onSurface
               )
           }
       }
   }

}