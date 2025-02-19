package com.example.dreamary.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.routes.NavRoutes

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
            .height(90.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Row (
            modifier = Modifier
                .weight(5f)
                .size(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
        }
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
            onClick = { navController.navigate(NavRoutes.Profile.route) },
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
                .clickable(
                    onClick = {
                        navController.navigate("burgerMenu")
                    }
                )
        )
    }
}