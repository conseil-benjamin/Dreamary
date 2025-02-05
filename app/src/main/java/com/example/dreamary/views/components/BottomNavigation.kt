package com.example.dreamary.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.ui.theme.DreamaryTheme

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    BottomNavigation(navController = NavController(LocalContext.current))
}

@Composable
fun BottomNavigation(navController: NavController) {
    var selected by remember { mutableIntStateOf(0) }

    DreamaryTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        selected = 0
                        navController.navigate("home")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    tint = if (selected == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = R.drawable.lune),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(id = R.string.BottomNavigation_journal),
                    fontSize = 14.sp
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        selected = 1
                        navController.navigate("home")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    tint = if (selected == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = if (selected == 1) R.drawable.progress_selected else R.drawable.progress),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(id = R.string.BottomNavigation_Stats),
                    fontSize = 14.sp
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopCenter)
                    .clickable {
                        selected = 2
                        navController.navigate("addDream")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    tint = if (selected == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = if (selected == 2) R.drawable.plus else R.drawable.plus),
                    contentDescription = "Home",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(shape = CircleShape)
                        .background(Color(0xFF3F51B5))
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        selected = 3
                        navController.navigate("homeSocial")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    tint = if (selected == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = if (selected == 3) R.drawable.users_selected else R.drawable.users),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(id = R.string.BottomNavigation_Social),
                    fontSize = 14.sp
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        selected = 4
                        navController.navigate("home")
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    tint = if (selected == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(id = if (selected == 4) R.drawable.book_selected else R.drawable.book),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(id = R.string.BottomNavigation_Guide),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}