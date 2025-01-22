package com.example.dreamary.views.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.R

@Preview(showBackground = true)
@Composable
fun TopNavigationPreview() {
    TopNavigation(navController = NavController(LocalContext.current))
}

@Composable
fun TopNavigation(navController: NavController) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ){
        Icon(
            painter = painterResource(id = R.drawable.lune),
            contentDescription = "Menu",
            modifier = Modifier
                .weight(1f)
                .size(24.dp)
        )
        Text(
            text = "Dreamary",
            modifier = Modifier
                .weight(4f)
                .size(24.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.search),
            contentDescription = "Menu",
            modifier = Modifier
                .weight(1f)
                .size(24.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.notification),
            contentDescription = "Menu",
            modifier = Modifier
                .weight(1f)
                .size(24.dp)
        )
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .weight(1f)
                .size(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Menu",
                modifier = Modifier
                    .size(24.dp)
            )
            Text(stringResource(id = R.string.TopNavigation))
        }
        Icon(
            painter = painterResource(id = R.drawable.menu),
            contentDescription = "Menu",
            modifier = Modifier
                .weight(1f)
                .size(24.dp)
        )
    }
}