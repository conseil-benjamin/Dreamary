package com.example.dreamary.views.components

import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Divider(
    color: Color = Color.Gray,
    thickness: Dp = 1 as Dp,
    modifier: Modifier = Modifier
) {
    Divider(
        color = color,
        thickness = thickness,
        modifier = modifier
    )
}