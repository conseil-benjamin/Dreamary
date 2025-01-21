package com.example.dreamary.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    BottomNavigation()
}

@Composable
fun BottomNavigation() {
    Row(

    ) {
        Column {
            Text(text = "Journal")
        }
    }
}