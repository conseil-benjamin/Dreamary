package com.example.dreamary.views.activities.Social

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.viewmodels.Social.SocialViewModel
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.Divider
import com.example.dreamary.views.components.Loading
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomePageSocialPreview() {
    HomePageSocialActivity(navController = NavController(LocalContext.current))
}


@Composable
fun HeaderSocial() {

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePageSocialActivity(navController: NavController, viewModel: SocialViewModel = viewModel()) {
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    Log.i("c", groups.toString())

    var research by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    Log.i("uid", currentUser!!.uid)

    LaunchedEffect(Unit) {
        viewModel.getGroupsForCurrentUser(currentUser.uid)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(5f),
                        text = "Social"
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.invite_people),
                        contentDescription = "Invite people",
                        modifier = Modifier
                            .size(24.dp)
                            .weight(0.5f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = research,
                        onValueChange = { research = it },
                        placeholder = {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.Social_Research_input),
                                modifier = Modifier
                                    .padding(start = 30.dp),
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = MaterialTheme.colorScheme.background),
                    )
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.users),
                            contentDescription = "Add post",
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Text(text = "Groupes")
                    }
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "Add post",
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Text(text = "Amis")
                    }
                }
                Divider()
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Mes groupes",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (groups.isNotEmpty()) {
                    for (group in groups) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column (
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                AsyncImage(
                                    model = group.image_url,
                                    contentDescription = null,
                                )
                            }
                            Column (
                                modifier = Modifier
                                    .weight(5f)
                            ) {
                                Row (
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
            }
        }
    }
}