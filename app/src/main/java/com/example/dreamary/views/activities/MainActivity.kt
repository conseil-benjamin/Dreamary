package com.example.dreamary.views.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.views.activities.auth.LoginActivity
import androidx.navigation.NavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamaryTheme {
                   LoginActivity(navController = NavController(this))
            }
        }
    }
}