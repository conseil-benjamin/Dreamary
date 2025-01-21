package com.example.dreamary.views.activities.Dreams

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@Preview(showBackground = true)
@Composable
fun AddDreamActivityPreview() {
    AddDreamActivity(navController = NavController(LocalContext.current))
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddDreamActivity (navController: NavController) {
    Text("AddDreamActivity")

    Scaffold (
        topBar = {
            // TopAppBar(title = { Text("Add Dream") })
        }
    ) {
        Text("AddDreamForm")
    }
}