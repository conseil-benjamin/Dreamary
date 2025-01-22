package com.example.dreamary.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.ui.theme.DreamaryTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Preview(showBackground = true)
@Composable
fun MenuBurgerPreview() {
    MenuBurger(navController = NavController(LocalContext.current))
}

@Composable
fun SimpleDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun Divider2 () {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun MenuBurger(navController: NavController) {
    val auth = Firebase.auth
    val context = LocalContext.current

    DreamaryTheme {
    Column (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.onSurface,
                painter = painterResource(id = R.drawable.lune_selected),
                contentDescription = "Menu",
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(4f)
            ) {
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    text = "Marie petit",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Niveau 12 - Rêveuse Expérimentée",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        SimpleDivider()
        Column {
            Text("Compte")
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .clip(RoundedCornerShape(8.dp)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Settings",
                        modifier = Modifier
                            .weight(0.5f)
                            .size(24.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(3f)
                    ) {
                        Text(
                            text= "Paramètres",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gérer votre compte",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Premium",
                        modifier = Modifier
                            .size(24.dp)
                            .weight(0.5f)
                    )
                }
                Divider2()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        painter = painterResource(id = R.drawable.premium),
                        contentDescription = "Premium",
                        modifier = Modifier
                            .weight(0.5f)
                            .size(24.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(3f)
                    ) {
                        Text(
                           text = "Premium",
                           style = MaterialTheme.typography.labelLarge,
                           color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Débloquez toutes les fonctionnalités",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Premium",
                        modifier = Modifier
                            .size(24.dp)
                            .weight(0.5f)
                    )
                }
            }
        }
        Column {
            Text("Navigation")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = R.drawable.lune),
                    contentDescription = "Menu",
                    modifier = Modifier
                        .weight(0.5f)
                        .size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(4f)
                ) {
                    Text(
                        text = "Journal des rêves",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tous vos rêves",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = R.drawable.book2),
                    contentDescription = "Menu",
                    modifier = Modifier
                        .weight(0.5f)
                        .size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(4f)
                ) {
                    Text(
                        text = "Guide Onirique",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Apprener à rêver lucidement",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Text("Support")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = R.drawable.help),
                    contentDescription = "Menu",
                    modifier = Modifier
                        .weight(0.5f)
                        .size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(4f)
                ) {
                    Text(
                        text = "Aide et Support",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        SimpleDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.error)
                .clip(RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                .clickable(
                        onClick = {
                            auth.signOut()
                            context.getSharedPreferences("isLoggedIn", 0).edit()
                                .putBoolean("isLoggedIn", false).apply()
                            navController.navigate("login") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }
                        }
                    ),
            ) {
                Row (
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ){
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Déconnexion",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    }
}