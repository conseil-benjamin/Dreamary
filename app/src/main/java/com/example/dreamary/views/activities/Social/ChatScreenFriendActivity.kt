package com.example.dreamary.views.activities.Social

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModel
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth

@Composable
fun HeaderChat(
    navController: NavController,
    userId: String,
    userUrlProfilePicture : String
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ){
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
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
                    .padding(8.dp)
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
fun ListOfMessages(
    messages: List<Message>,
    userId: String
) {
    // todo : afficher les messages
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(messages.size) { index ->
            val message = messages[index]
            val isCurrentUser = message.senderId == userId
            if (isCurrentUser) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    CardMessage(
                        message = message,
                        userId = userId
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    CardMessage(
                        message = message,
                        userId = userId
                    )
                }
            }
        }
    }
}

@Composable
fun CardMessage(
    message: Message,
    userId: String
) {
    val timeStamp = message.createdAt.toDate().time
    val currentTime = System.currentTimeMillis()
    val timePassed = (currentTime - timeStamp) / 1000
    val timePassedInMinutes = timePassed / 60
    val timePassedInHours = timePassedInMinutes / 60
    val timePassedInDays = timePassedInHours / 24

    Card (
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (message.senderId != userId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)

    ){
        Row (
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(8.dp)
        ){
            Text(text = message.content)
        }
        Row (
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(text = when {
                timePassedInDays > 0 -> "$timePassedInDays jours"
                timePassedInHours > 0 -> "$timePassedInHours heures"
                timePassedInMinutes > 0 -> "$timePassedInMinutes minutes"
                else -> "$timePassed secondes"
            },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            )
        }
    }
}

@Composable
fun MessageField(
    userId: String,
    message: String,
    onMessageChange: (String) -> Unit,
    onMessageSend: (Message) -> Unit,
) {
    val currentUser = Firebase.auth.currentUser
    var sender = currentUser?.uid ?: ""
    var receiver = userId

    if (currentUser?.uid == userId){
        sender = userId
        receiver = currentUser.uid
    } else {
        sender = currentUser?.uid ?: ""
        receiver = userId
    }

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
            ),
            value = message,
            placeholder = { Text("Votre message...") },
            onValueChange = {onMessageChange(it)},
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
                            // todo
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
                    onMessageSend(
                        Message(
                            senderId = sender,
                            receiverId = receiver,
                            content = message,
                            dreamId = "",
                            type = "text",
                            seen = false,
                            createdAt = Timestamp.now()
                        )
                    )
                }
        )
    }
}

@Composable
fun ChatScreenFriendActivity(
    navController: NavController,
    userId: String,
    userUrlProfilePicture: String,
    chatId: String,
    viewModel: ChatScreenFriendViewModel = viewModel(
        factory = ChatScreenFriendViewModelFactory(
            socialRepository = SocialRepository(
                LocalContext.current
            )
        )
    )
) {
    var messageToBeSend by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    var message by remember { mutableStateOf(Message()) }

    LaunchedEffect (Unit){
        viewModel.getMessagesForCurrentUser(chatId)
    }

    LaunchedEffect(messages){
        Log.i("messages", messages.toString())
        viewModel.getMessagesForCurrentUser(chatId)
    }

    // todo : récupérer les messages
    // todo : récupérer les informations de l'utilisateur
    DreamaryTheme {
    Scaffold (
        bottomBar = {
            MessageField(userId, messageToBeSend, onMessageChange = { messageToBeSend = it }, onMessageSend = { message ->
                viewModel.sendMessage(chatId, message)
                messageToBeSend = ""
            })
        },
        topBar = {
            HeaderChat(
                navController = navController,
                userId = userId,
                userUrlProfilePicture = userUrlProfilePicture,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            ListOfMessages(
                messages = messages,
                userId = userId
            )
        }
    }
    }
}