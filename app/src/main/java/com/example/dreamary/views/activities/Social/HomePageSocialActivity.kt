package com.example.dreamary.views.activities.Social

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.R
import com.example.dreamary.models.entities.Conversation
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.viewmodels.Social.SocialViewModel
import com.example.dreamary.viewmodels.profile.SocialViewModelFactory
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.Divider
import com.example.dreamary.views.components.Loading
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun ShowConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String,
    title: String,
    confirmText: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePageSocialActivity(
    navController: NavController,
    viewModel: SocialViewModel = viewModel(
        factory = SocialViewModelFactory(SocialRepository(LocalContext.current))
    ),
) {
    val groups by viewModel.groups.collectAsState()
    val users by viewModel.users.collectAsState()
    val friends by viewModel.listFriends.collectAsState()
    val conversations by viewModel.listConversations.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var research by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Groupes", "Messages", "Amis")

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        viewModel.getProfileData(currentUser?.uid ?: "")
        viewModel.getGroupsForCurrentUser(currentUser?.uid ?: "")
        viewModel.getFriendsForCurrentUser(currentUser?.uid ?: "")
        viewModel.getConversationsForCurrentUser(currentUser?.uid ?: "")
        viewModel.getFriendRequestsForCurrentUser(currentUser?.uid ?: "")
    }

    LaunchedEffect(friendRequests) {
        viewModel.getFriendsForCurrentUser(currentUser?.uid ?: "")
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        SnackbarManager.snackbarMessages.collect { snackbarMessage ->
            snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = snackbarMessage.actionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    DreamaryTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { BottomNavigation(navController = navController) },
            topBar = {
                SocialAppBar(
                    research = research,
                    onResearchChange = { research = it },
                    onStopTyping = { viewModel.searchUsers(research) },
                    users = users,
                    navController = navController,
                    hasRequests = friendRequests.isNotEmpty()
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Loading()
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        SocialTab(
                            title = title,
                            selected = selectedTab == index,
                            hasNotification = index == 2 && friendRequests.isNotEmpty(),
                            onClick = { selectedTab = index }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> GroupsContent(groups = groups, navController = navController)
                    1 -> ConversationsContent(
                        navController = navController,
                        conversations = conversations,
                        userId = currentUser?.uid ?: ""
                    )
                    2 -> FriendsContent(
                        userData = userData,
                        friendRequests = friendRequests,
                        friends = friends,
                        navController = navController,
                        onCreateConversation = { conversation ->
                            viewModel.createConversation(conversation, onConversationCreated = { chatId ->
                                navController.navigate(
                                    NavRoutes.ChatScreenFriends.createRoute(
                                        conversation.userId2,
                                        conversation.user2.profilePictureUrl,
                                        chatId
                                    )
                                )
                            })
                        },
                        onUpdateFriendRequest = { userId, friendId, status ->
                            viewModel.updateFriendRequest(userId, friendId, status)
                        },
                        onFriendDelete = { friendId ->
                            viewModel.deleteFriend(currentUser?.uid ?: "", friendId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SocialTab(
    title: String,
    selected: Boolean,
    hasNotification: Boolean = false,
    onClick: () -> Unit
) {
    val indicator = @Composable {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        if (hasNotification) {
            BadgedBox(
                badge = {
                    Badge(
                        modifier = Modifier.size(8.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    )
                }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAppBar(
    research: String = "",
    onResearchChange: (String) -> Unit = {},
    onStopTyping: (String) -> Unit,
    users: List<User> = emptyList(),
    navController: NavController,
    hasRequests: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title + Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Social",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
//                BadgedBox(
//                    badge = {
//                        // Ensure AnimatedVisibility is correctly scoped
//                        AnimatedVisibility(
//                            visible = hasRequests,
//                            enter = fadeIn(),
//                            exit = fadeOut()
//                        ) {
//                            Badge(
//                                modifier = Modifier.size(8.dp),
//                                containerColor = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.invite_people),
//                        contentDescription = "Invite people",
//                        modifier = Modifier
//                            .size(28.dp)
//                            .clip(CircleShape)
//                            .clickable { /* Handle invite click */ }
//                            .padding(4.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar with Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(12.dp))
                    .shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                OutlinedTextField(
                    value = research,
                    onValueChange = {
                        onResearchChange(it)

                        // Cancel old delay and start a new one
                        debounceJob?.cancel()
                        debounceJob = coroutineScope.launch {
                            delay(500) // Wait 0.5 seconds before triggering the action
                            if (it.isNotEmpty()) {
                                expanded = true
                                onStopTyping(it)
                            } else {
                                expanded = false
                            }
                        }
                    },
                    placeholder = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.Social_Research_input),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(), // Important to link the menu to TextField
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Dropdown menu
                if (expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxWidth()
                    ) {
                        if (users.isEmpty() && research.isNotEmpty()) {
                            DropdownMenuItem(
                                modifier = Modifier.fillMaxWidth(),
                                text = {
                                    Text(
                                        text = "Aucun utilisateur trouvé",
                                        textAlign = TextAlign.Center,
                                    )
                                },
                                onClick = { expanded = false }
                            )
                        } else {
                            users.forEach { user ->
                                DropdownMenuItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = user.profilePictureUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = user.username,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = user.fullName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        expanded = false
                                        navController.navigate(
                                            NavRoutes.Profile.createRoute(user.uid)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsContent(groups: List<Group>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        item {
            CreateGroupButton()
        }

        item {
            JoinGroupInput()
        }

        item {
            SectionTitle(title = "Mes Groupes", count = groups.size)
        }

        if (groups.isNotEmpty()) {
            items(groups.size) { index ->
                GroupCard(group = groups[index], navController = navController)
            }
        } else {
            item {
                EmptyStateMessage(message = "Vous n'avez pas encore de groupe")
            }
        }
    }
}

@Composable
fun ConversationsContent(
    navController: NavController,
    conversations: List<Conversation>,
    userId: String,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        if (conversations.isNotEmpty()) {
            items(conversations.size) { index ->
                ConversationCard(
                    conversation = conversations[index],
                    userId = userId,
                    navController = navController
                )
            }
        } else {
            item {
                EmptyStateMessage(message = "Vous n'avez pas encore de conversations")
            }
        }
    }
}

@Composable
fun FriendsContent(
    userData: User?,
    friendRequests: List<User>,
    friends: List<User>,
    navController: NavController,
    onCreateConversation: (Conversation) -> Unit,
    onUpdateFriendRequest: (String, String, String) -> Unit,
    onFriendDelete: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    Log.i("friends", friendRequests.toString())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        if (friendRequests.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Demandes d'amis",
                    count = friendRequests.size,
                    showCount = true
                )
            }

            items(friendRequests.size) { index ->
                FriendRequestCard(
                    user = friendRequests[index],
                    onAccept = {
                        onUpdateFriendRequest(
                            auth.currentUser?.uid ?: "",
                            friendRequests[index].uid,
                            "accepted"
                        )
                    },
                    onDecline = {
                        onUpdateFriendRequest(
                            auth.currentUser?.uid ?: "",
                            friendRequests[index].uid,
                            "refuse"
                        )
                    }
                )
            }
        }

        item {
            SectionTitle(title = "Amis", count = friends.size)
        }

        if (friends.isNotEmpty()) {
            items(friends.size) { index ->
                FriendCard(
                    friend = friends[index],
                    navController = navController,
                    onMessageClick = {
                        onCreateConversation(
                            Conversation(
                                user1 = userData,
                                user2 = friends[index],
                                lastMessage = "",
                                lastMessageTimestamp = Timestamp.now(),
                                lastSender = "",
                                unreadMessagesUser1 = 0,
                                unreadMessagesUser2 = 0,
                                chatId = userData?.uid.toString() + friends[index].uid,
                                userId1 = auth.currentUser?.uid ?: "",
                                userId2 = friends[index].uid
                            )
                        )
                    },
                    onFriendDelete = { friendId ->
                        onFriendDelete(friendId)
                    }
                )
            }
        } else {
            item {
                EmptyStateMessage(message = "Vous n'avez pas encore d'amis")
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, count: Int, showCount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        if (showCount && count > 0) {
            Badge(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = count.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun CreateGroupButton() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(60.dp)
            .clickable { /* Handle create group click */ }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = "Create group",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Créer un groupe",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun JoinGroupInput() {
    var input by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(2f),
            value = input,
            onValueChange = {
                input = it
                isError = false
                errorMessage = ""
            },
            label = { Text("Code du groupe privé") },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        Button(
            onClick = { /* Handle join group click */ },
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        ) {
            Text(text = "Rejoindre")
        }
    }

}

@Composable
fun GroupCard(group: Group, navController: NavController) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* Navigate to group details */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = group.image_url,
                contentDescription = "Group image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        painter = painterResource(
                            id = if (group.privacy == "private") R.drawable.lock
                            else R.drawable.privacy_public
                        ),
                        contentDescription = "Privacy status",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${group.members.size} membres",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConversationCard(
    conversation: Conversation,
    userId: String,
    navController: NavController
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    val timestamp = conversation.lastMessageTimestamp.toDate()
    val currentTime = Date()
    val isToday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(timestamp) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime)

    val formattedTime = if (isToday) {
        timeFormatter.format(timestamp)
    } else {
        dateFormatter.format(timestamp)
    }

    val otherUser = if (userId == conversation.user1?.uid) conversation.user2 else conversation.user1
    val otherUserPicture = if (otherUser?.profilePictureUrl != null) otherUser.profilePictureUrl else ""
    Log.i("otherUser", otherUser.toString())
    Log.i("otherUserPicture", otherUserPicture.toString())
    Log.i("userId50", userId)
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate(
                    NavRoutes.ChatScreenFriends.createRoute(
                        otherUser?.uid ?: "",
                        otherUserPicture.toString(),
                        conversation.chatId
                    )
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = otherUserPicture,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = otherUser?.username ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message preview with unread indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Add unread indicator if needed
                    val hasUnread = (userId == conversation.userId1 && conversation.unreadMessagesUser1 > 0) ||
                            (userId == conversation.userId2 && conversation.unreadMessagesUser2 > 0)

                    Log.i("unread", hasUnread.toString())

                    Text(
                        text = if (conversation.lastMessage.isNotEmpty())
                            conversation.lastMessage
                        else "Démarrer une conversation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasUnread)
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (hasUnread) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userId == conversation.userId1) conversation.unreadMessagesUser1.toString() else conversation.unreadMessagesUser2.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    user: User,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ShowConfirmDialog(
            onConfirm = onDecline,
            onDismiss = { showConfirmDialog = false },
            text = "Vous êtes sur le point de refuser la demande d'ami de ${user.username}",
            title = "Refuser la demande d'ami",
            confirmText = "Refuser"
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accept button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAccept() },
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.check_circle),
                        contentDescription = "Accept friend request",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                // Decline button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable {
                            showConfirmDialog = true
                                   },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = "Decline friend request",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                }

            }
        }
    }
}


@Composable
fun FriendCard(
    friend: User,
    navController: NavController,
    onMessageClick: () -> Unit,
    onFriendDelete: (String) -> Unit
) {
    var showDropdownMoreActions by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ShowConfirmDialog(
            onConfirm = { onFriendDelete(friend.uid) },
            onDismiss = { showConfirmDialog = false },
            text = "Vous êtes sur le point de supprimer votre ami.",
            title = "Supprimer l'ami",
            confirmText = "Supprimer"
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = friend.profilePictureUrl,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        navController.navigate(
                            NavRoutes.Profile.createRoute(friend.uid)
                        )
                    }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "@${friend.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.comment),
                contentDescription = "Send message",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onMessageClick() },
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Icon(
                painter = painterResource(id = R.drawable.dots),
                contentDescription = "More options",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        showDropdownMoreActions = !showDropdownMoreActions
                    },
                tint = MaterialTheme.colorScheme.onPrimary
            )
            if (showDropdownMoreActions) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Supprimer l'ami",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable{
                                    showConfirmDialog = true
                                }
                        )
                    },
                    onClick = { showDropdownMoreActions = false }
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(16.dp)
    )
}


