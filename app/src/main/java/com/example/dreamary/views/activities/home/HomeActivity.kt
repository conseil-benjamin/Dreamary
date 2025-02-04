package com.example.dreamary.views.activities.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.CustomSnackbarHost
import com.example.dreamary.views.components.TopNavigation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Preview(showBackground = true)
@Composable
private fun PreviewHomeActivity() {
    HomeActivity(navController = NavController(LocalContext.current))
}

@Composable
fun HomeActivity(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }

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

    DreamaryTheme {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onSurface),
            bottomBar = { BottomNavigation(navController = navController) },
            topBar = { TopNavigation(navController = navController) },
            snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
            }
        }
    }
}

