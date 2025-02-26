package com.example.dreamary.views.activities.Social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme

@Composable
fun HeaderChat(
    navController: NavController,
    userId: String,
    userUrlProfilePicture : String,
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ){
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .size(24.dp)
                .clickable {
                    navController.popBackStack()
                }
        )
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clickable {
                    navController.navigate(NavRoutes.Profile.createRoute(userId))
                }
                .weight(5f)
        ){
            AsyncImage(
                model = userUrlProfilePicture,
                contentDescription = "avatar",
                modifier = Modifier
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .align(Alignment.CenterVertically)
                    .size(24.dp)

            )
            Column {
                // todo : afficher le nom de l'utilisateur
                // todo : afficher le statut de l'utilisateur
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = "En ligne",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun ListOfMessages() {
    // todo : afficher les messages

}

@Composable
fun MessageField(
    message: String,
    onMessageChange: (String) -> Unit
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ){
        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            ),            value = "",
            placeholder = { Text("Votre message...") },
            onValueChange = {},
            label = { Text("Message") },
            modifier = Modifier
                .clip(CircleShape)
                .padding(16.dp),
            trailingIcon = {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = R.drawable.picture),
                    contentDescription = "envoyer",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable {
                            // todo : envoyer le message
                        }
                )
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = R.drawable.microphone),
                    contentDescription = "voix",
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 16.dp)
                        .clickable {
                            // todo : envoyer le message
                        }
                )
            }
        )
        Icon(
            painter = painterResource(id = R.drawable.send),
            contentDescription = "envoyer",
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    // todo : envoyer le message
                }
        )
    }
}

@Composable
fun ChatScreenFriendActivity(
    navController: NavController,
    userId: String,
    userUrlProfilePicture: String
) {
    var messageToBeSend by remember { mutableStateOf("") }

    // todo : récupérer les messages
    // todo : récupérer les informations de l'utilisateur
    DreamaryTheme {
    Scaffold (
        bottomBar = {
            MessageField(messageToBeSend, onMessageChange = { messageToBeSend = it })
        },
        topBar = {
            HeaderChat(
                navController = navController,
                userId = userId,
                userUrlProfilePicture = userUrlProfilePicture
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            ListOfMessages()
            // todo : afficher le champ de saisie
        }
    }
    }
}