package com.example.dreamary.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dreamary.utils.SnackbarMessage

@Composable
fun CustomSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        val currentSnackbarData = remember { SnackbarMessage("", 0) }


        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
//                if (data.actionLabel != null) {
//                    TextButton(onClick = { data.performAction() }) {
//                        Text(
//                            text = data.actionLabel,
//                            color = MaterialTheme.colorScheme.inversePrimary
//                        )
//                    }
//                }
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = currentSnackbarData.iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(text = data.visuals.message)
            }
        }
    }
}