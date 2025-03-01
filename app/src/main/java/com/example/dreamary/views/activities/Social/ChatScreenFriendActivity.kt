package com.example.dreamary.views.activities.Social

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModel
import com.example.dreamary.viewmodels.Social.ChatScreenFriendViewModelFactory
import com.example.dreamary.views.components.Loading
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HeaderChat(
    friendInformation: User?,
    navController: NavController,
    userId: String,
    userUrlProfilePicture: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(NavRoutes.Profile.createRoute(userId))
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = friendInformation?.profilePictureUrl ?: userUrlProfilePicture,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column {
                    Text(
                        text = friendInformation?.username ?: "Utilisateur",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val userStatus = friendInformation?.bio != "" ?: false
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (userStatus) Color.Green else Color.Gray)
                        )
                        Text(
                            text = if (userStatus) "En ligne" else "Hors ligne",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            IconButton(onClick = { /* Menu options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
fun DateDivider(date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = date,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun ListOfMessages(
    messages: List<Message>,
    userId: String,
    listState: LazyListState
) {
    LazyColumn(
        reverseLayout = false,
        state = listState,
        modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
    ) {
    if (messages.isNotEmpty()) {
        // Prétraiter les messages pour créer une liste d'éléments à afficher
        val itemsToDisplay = mutableListOf<Any>()
        var currentDate = ""

        messages.forEach { message ->
            val messageDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(message.createdAt.toDate())

            if (messageDate != currentDate) {
                currentDate = messageDate
                itemsToDisplay.add("DATE:$currentDate") // Marqueur de date
            }
            itemsToDisplay.add(message) // Message normal
        }

        // Afficher les éléments
        items(itemsToDisplay.size) { index ->
            val item = itemsToDisplay[index]

            when (item) {
                is String -> {
                    if (item.startsWith("DATE:")) {
                        DateDivider(date = item.substringAfter("DATE:"))
                    }
                }
                is Message -> {
                    val isCurrentUser = item.senderId == userId

                    Row(
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        MessageBubble(
                            message = item,
                            isCurrentUser = isCurrentUser
                        )
                    }
                }
            }
        }
    } else {
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Text(
                    text = "Commencez à discuter 💬",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
        bottomEnd = if (isCurrentUser) 4.dp else 16.dp
    )

    val bubbleColor = if (isCurrentUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isCurrentUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = formatter.format(message.createdAt.toDate())

    Column(
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                modifier = Modifier.padding(12.dp)
            )
        }

        Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 8.dp)
        )
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
    val sender = currentUser?.uid ?: ""
    val receiver = if (currentUser?.uid == userId) currentUser.uid else userId

    var isTyping by remember { mutableStateOf(false) }
    isTyping = message.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Attachment button
            IconButton(
                onClick = { /* Open attachment options */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.picture),
                    contentDescription = "Joindre",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Message input field
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                value = message,
                placeholder = { Text("Votre message...") },
                onValueChange = { onMessageChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    AnimatedVisibility(visible = !isTyping) {
                        IconButton(onClick = { /* Open voice recording */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.microphone),
                                contentDescription = "Enregistrer",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )

            // Send button
            IconButton(
                onClick = {
                    if (message.isNotEmpty()) {
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
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isTyping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Envoyer",
                    tint = if (isTyping) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
    val lazyListState = rememberLazyListState()
    val friendInformation by viewModel.friendInformation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMessagesForCurrentUser(chatId)
        viewModel.getFriendInformation(userId)
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    DreamaryTheme {
        Scaffold(
            bottomBar = {
                if (!isLoading) {
                    MessageField(
                        userId = userId,
                        message = messageToBeSend,
                        onMessageChange = { messageToBeSend = it },
                        onMessageSend = { message ->
                            viewModel.sendMessage(chatId, message)
                            messageToBeSend = ""
                        }
                    )
                }
            },
            topBar = {
                if (!isLoading) {
                    HeaderChat(
                        friendInformation = friendInformation,
                        navController = navController,
                        userId = userId,
                        userUrlProfilePicture = userUrlProfilePicture,
                    )
                }
            }
        ) { paddingValues ->
            if (isLoading) {
                Loading()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ListOfMessages(
                        messages = messages,
                        userId = Firebase.auth.currentUser?.uid ?: "",
                        listState = lazyListState
                    )
                }
            }
        }
    }
}