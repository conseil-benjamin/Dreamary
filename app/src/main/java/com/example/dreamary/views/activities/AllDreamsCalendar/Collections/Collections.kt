package com.example.dreamary.views.activities.AllDreamsCalendar.Collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.entities.User
import com.example.dreamary.ui.theme.LightOnSurface
import com.example.dreamary.ui.theme.Primary
import com.example.dreamary.ui.theme.Secondary

@Composable
fun UserNotPremium(
    navController: NavController,
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        HeaderCollections()
        Column (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.premium),
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp),
                contentDescription = "Image",
            )
            Text(
                text = "Fonctionnalité Premium",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Accédez à des fonctionnalités exclusives et améliorez votre expérience de rêve.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
            )

            Text(
                text = "En savoir plus",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("premium")
                    }
            )
        }
    }
}

@Composable
fun HeaderCollections() {
    Text(
        text = "Collections",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = LightOnSurface // Text color for better contrast
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Primary, // Violet lavande
                        Secondary // Ocre doré
                    )
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 8.dp)
    )
}
@Composable
fun Collections(
    navController: NavController,
    userData: User?
) {
    if (userData?.metadata?.get("isPremium") != false) {
        Column {
            HeaderCollections()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        navController.navigate("collections")
                    }
            ) {
                AsyncImage(
                    model = userData?.metadata?.get("imageUrl"),
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp)
                )
                Text(
                    text = "Mes Collections",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    } else {
        UserNotPremium(
            navController = navController
        )
    }
}