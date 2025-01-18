package com.example.dreamary.views.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.navigation.NavigationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamaryTheme {
                NavigationManager()
            }
        }
    }
}