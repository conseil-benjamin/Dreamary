package com.example.dreamary.views.activities.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomeActivity(navController: NavController) {
    val auth = Firebase.auth

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            auth.signOut()
            navController.navigate("login")
        }) {
            Text("Sign Out")
        }

        Text("HomeActivity")
    }
}

