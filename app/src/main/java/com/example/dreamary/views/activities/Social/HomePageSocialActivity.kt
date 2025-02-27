package com.example.dreamary.views.activities.Social

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dreamary.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.models.entities.Conversation
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.viewmodels.Social.SocialViewModel
import com.example.dreamary.viewmodels.profile.SocialViewModelFactory
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.Divider
import com.example.dreamary.views.components.Loading
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomePageSocialPreview() {
    HomePageSocialActivity(navController = NavController(LocalContext.current))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSocial(
    research: String = "",
    onResearchChange: (String) -> Unit = {},
    onStopTyping: (String) -> Unit,
    users: List<User> = emptyList(),
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Titre + Icone
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Social",
                style = MaterialTheme.typography.titleLarge
            )

            Icon(
                painter = painterResource(id = R.drawable.invite_people),
                contentDescription = "Invite people",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barre de recherche avec Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            OutlinedTextField(
                value = research,
                onValueChange = {
                    onResearchChange(it)

                    // Annule l'ancien délai et en lance un nouveau
                    debounceJob?.cancel()
                    debounceJob = coroutineScope.launch {
                        delay(1000) // Attendre 1 seconde avant de déclencher l'action
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.Social_Research_input),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // Important pour relier le menu au TextField
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Menu déroulant
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
            ) {
                if (users.isEmpty() && research.isNotEmpty() && expanded) {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        text = { Text(
                            text = "Aucun utilisateur trouvé",
                            textAlign = TextAlign.Center,
                        ) },
                        onClick = { expanded = false }
                    )
                } else {
                    users.forEach { user ->
                        DropdownMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            text = { Text(user.username) },
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePageSocialActivity(
    navController: NavController,
    viewModel: SocialViewModel = viewModel(
    factory = SocialViewModelFactory (SocialRepository(LocalContext.current))
),
    ) {
    val groups by viewModel.groups.collectAsState()
    val users by viewModel.users.collectAsState()
    val friends by viewModel.listFriends.collectAsState()
    val conversations by viewModel.listConversations.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    Log.i("c", groups.toString())

    var research by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    Log.i("uid", currentUser!!.uid)

    var socialChoose by remember { mutableStateOf("groupes") }

    LaunchedEffect(Unit) {
        viewModel.getGroupsForCurrentUser(currentUser.uid)
        viewModel.getFriendsForCurrentUser(currentUser.uid)
        viewModel.getConversationsForCurrentUser(currentUser.uid)
        viewModel.getFriendRequestsForCurrentUser(currentUser.uid)
    }

    DreamaryTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { BottomNavigation(navController = navController) }
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
                HeaderSocial(
                    research = research,
                    onResearchChange = { research = it },
                    onStopTyping = { viewModel.searchUsers(research) },
                    users = users,
                    navController = navController
                )
                Divider()
                BoutonGroupesAndFriends(onSocialChooseChange = { socialChoose = it })
                if (socialChoose == "groupes"){
                    ButtonSocial()
                    LazyColumn {
                        item { Groupes(groups = groups) }
                    }
                } else if (socialChoose == "friends") {
                    LazyColumn {
                        item {
                            Friends(
                                friendRequests = friendRequests,
                                friends = friends,
                                navController = navController,
                                onCreateConversation = { conversation ->
                                    viewModel.createConversation(conversation)
                                },
                                onUpdateFriendRequest = { userId, friendId, status ->
                                    viewModel.updateFriendRequest(userId, friendId, status)
                                }
                                )
                        }
                    }
                } else {
                    Conversations(
                        navController = navController,
                        conversations = conversations,
                        userId = currentUser.uid,
                    )
                }
            }
        }
    }
}

@Composable
fun Conversations(
    navController: NavController,
    conversations: List<Conversation>,
    userId: String,
) {
    LazyColumn {
        for (conversation in conversations) {
            val timeStamp = conversation.lastMessageTimestamp.toDate().time
            Log.i("timeStamp", timeStamp.toString())
            val currentTime = System.currentTimeMillis()
            val timePassed = (currentTime - timeStamp) / 1000
            Log.i("timePassed", timePassed.toString())
            val timePassedInMinutes = timePassed / 60
            val timePassedInHours = timePassedInMinutes / 60
            val timePassedInDays = timePassedInHours / 24

            item{
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate(NavRoutes.ChatScreenFriends.createRoute(
                                if (userId == conversation.user1) conversation.user2 else conversation.user1,
                                if (userId == conversation.user1) conversation.profilePictureUser2 else conversation.profilePictureUser1,
                                conversation.chatId
                            ))
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            AsyncImage(
                                model = if (userId == conversation.user1) conversation.profilePictureUser2 else conversation.profilePictureUser1,
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (conversation.user1 == userId) {
                                    Text(
                                        text = conversation.user2,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                } else {
                                    Text(
                                        text = conversation.user1,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = conversation.lastMessage,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = when {
                                        timePassedInDays > 0 -> "$timePassedInDays jours"
                                        timePassedInHours > 0 -> "$timePassedInHours heures"
                                        timePassedInMinutes > 0 -> "$timePassedInMinutes minutes"
                                        else -> "$timePassed secondes"
                                    },
                                    style = MaterialTheme.typography.bodySmall
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
fun BoutonGroupesAndFriends(
    onSocialChooseChange: (String) -> Unit
) {
    Row (
        modifier = Modifier.fillMaxWidth()
    ){
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .clickable { onSocialChooseChange("groupes") }
        ) {
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.users),
                    contentDescription = "Groupes",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Groupes",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .clickable { onSocialChooseChange("conversation") }
        ) {
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.messages),
                    contentDescription = "conversation",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .clickable { onSocialChooseChange("friends") }
        ) {
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Amis",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Amis",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ButtonSocial() {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = "Create group",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Créer un groupe",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun Friends(
    friendRequests: List<User>,
    friends: List<User>,
    navController: NavController,
    onCreateConversation: (Conversation) -> Unit,
    onUpdateFriendRequest: (String, String, String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    if (friendRequests.isNotEmpty()){
        Text(
            text = "Demandes d'amis",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Column {
            for (friendRequest in friendRequests){
                Card (
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column (
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ){
                            AsyncImage(
                                model = friendRequest.profilePictureUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .fillMaxSize()
                            )
                        }
                        Column (
                            modifier = Modifier
                                .weight(5f)
                        ) {
                            Text(
                                text = friendRequest.fullName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "@${friendRequest.username}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row (
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.check_circle),
                                contentDescription = "Accepter",
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 16.dp)
                                    .clickable {
                                        onUpdateFriendRequest(
                                            auth.currentUser!!.uid,
                                            friendRequest.uid,
                                            "accepted"
                                        )
                                    }
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.check_circle),
                                contentDescription = "Refuser",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        onUpdateFriendRequest(
                                            auth.currentUser!!.uid,
                                            friendRequest.uid,
                                            "refuse"
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
    Text(
        text = "Amis",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )
    if (friends.isNotEmpty()) {
        for (friend in friends){
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row (
                    modifier = Modifier
                        .clickable{
                            navController.navigate(NavRoutes.Profile.createRoute(friend.uid))
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        AsyncImage(
                            model = friend.profilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = friend.fullName,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "@${friend.username}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                Column (
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.comment),
                        contentDescription = "Message",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                // todo : d'abord créer la conversation et après rediriger vers la conversation en passant les variables friends
                               onCreateConversation(
                                   Conversation(
                                    user1 = auth.currentUser!!.uid,
                                    user2 = friend.uid,
                                    profilePictureUser1 = friend.profilePictureUrl,
                                    profilePictureUser2 = "",
                                    lastMessage = "",
                                    lastMessageTimestamp = Timestamp.now(),
                                    lastSender = "",
                                    unreadMessagesUser1 = 0,
                                    unreadMessagesUser2 = 0,
                                    chatId = auth.currentUser!!.uid + friend.uid,
                                ),
                               )
                            }
                    )
                }
            }
        }
    } } else {
        Text(
            text = "Vous n'avez pas encore d'amis",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Groupes(groups: List<Group>) {

    Text(
        text = "Mes Groupes",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )

    if (groups.isNotEmpty()) {
        for (group in groups) {
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        AsyncImage(
                            model = group.image_url,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (true) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Icon(
                                painter = painterResource(id = if (group.privacy == "private") R.drawable.lock else R.drawable.privacy_public),
                                contentDescription = "Invite people",
                                modifier = Modifier
                                    .size(16.dp)
                            )
                        }
                        Text(
                            text = "${group.members.size} membres",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = "Vous n'avez pas encore de groupe",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}