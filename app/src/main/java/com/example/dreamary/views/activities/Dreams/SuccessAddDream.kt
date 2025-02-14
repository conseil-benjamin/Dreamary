package com.example.dreamary.views.activities.Dreams

import android.content.Context
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson

@Composable
fun SuccessAddDream(
    navController: NavController,
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val user = sharedPreferences.getString("user", null)
    val gson = Gson()
    var userObject = gson.fromJson(user, User::class.java)

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
    ) { paddingValues ->
        LazyColumn (
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ){
            item {
                HeaderSuccess()
            }
            item{
                TwoCardsStats(
                    user = userObject
                )
            }
            item {
                Bottom(
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun HeaderSuccess() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.success),
            contentDescription = "Fire icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Rêve ajouté avec succès",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun itemFirstCard(
    title: String,
    subtitle: String,
    icon: Int
) {
    Row {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Star icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TwoCardsStats(
    user: User
) {
    val xp: Long = user?.progression?.get("xp") as Long
    val xpNeeded: Long = user?.progression?.get("xpNeeded") as Long
    val progess = xp.toFloat() / xpNeeded.toFloat()

    Column {
        Card {
            itemFirstCard(
                title = "Série actuelle",
                subtitle = user.dreamStats["currentStreak"].toString(),
                icon = R.drawable.fire
            )
            itemFirstCard(
                title = "Total de rêves",
                subtitle = user.dreamStats["totalDreams"].toString(),
                icon = R.drawable.fire
            )
        }
        Card {
            Row {
                Icon(
                    painter = painterResource(id = R.drawable.fire),
                    contentDescription = "Star icon",
                    tint = MaterialTheme.colorScheme.primary

                )
                Column {
                    Text(
                        text = user.progression["level"].toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = progess,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${user.progression["xp"]} / ${user.progression["xpNeeded"]}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun Bottom(navController: NavController) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                navController.navigate(NavRoutes.Home.route) {
                    popUpTo(NavRoutes.SucessAddDream.route) {
                        inclusive = true
                    }
                }
            }
        ) {
            Text(
                text = "Retour à l'accueil"
            )
        }
    }
}