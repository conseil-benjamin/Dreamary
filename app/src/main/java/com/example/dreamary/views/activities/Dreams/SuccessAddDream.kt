package com.example.dreamary.views.activities.Dreams

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.google.gson.Gson

@Composable
fun SuccessAddDream(
    navController: NavController,
) {

    // todo : afficher quand un utilisateur à gagner un nouveau badge
    // todo : donc afficher le badge avec le nom, sa rareté etc.
    // todo : pourquoi pas également aussi ajouter une information pour que l'utilisateur
    // todo : sache qu'il vient tout juste monter en niveau

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("userDatabase", Context.MODE_PRIVATE)
    val userJson = sharedPreferences.getString("userDatabase", null)
    val gson = Gson()
    val userObject = gson.fromJson(userJson, User::class.java)

    DreamaryTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                item { HeaderSuccess() }
                item { TwoCardsStats(user = userObject) }
                item { Bottom(navController = navController) }
            }
        }
    }
}

@Composable
fun HeaderSuccess() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.success),
            contentDescription = "Success icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Rêve ajouté avec succès 🎉",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 22.sp
        )
    }
}

@Composable
fun TwoCardsStats(user: User) {
    val xp = (user.progression["xp"] as? Number)?.toFloat() ?: 0f
    val xpNeeded = (user.progression["xpNeeded"] as? Number)?.toFloat() ?: 1f
    val progress = xp / xpNeeded

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ItemCardStat(title = "Série actuelle", subtitle = user.dreamStats["currentStreak"].toString(), icon = R.drawable.fire)
                ItemCardStat(title = "Total de rêves", subtitle = user.dreamStats["totalDreams"].toString(), icon = R.drawable.lune)
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.progress),
                        contentDescription = "Progress icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Progression", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "Niveau ${user.progression["level"]}",
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.LightGray
                )
                Text(
                    text = "${user.progression["xp"]} / ${user.progression["xpNeeded"]} XP",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun ItemCardStat(title: String, subtitle: String, icon: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun Bottom(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                navController.navigate(NavRoutes.Home.route) {
                    popUpTo(NavRoutes.SucessAddDream.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Retour à l'accueil", fontSize = 16.sp)
        }
    }
}
