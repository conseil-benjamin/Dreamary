package com.example.dreamary.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Row (
            modifier = Modifier
                .weight(5f)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.dreamary_name2),
                contentDescription = "Menu",
                modifier = Modifier
                    .padding(start = 16.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
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
        Image(
            painter = painterResource(id = R.drawable.user),
            contentDescription = "Menu",
            modifier = Modifier
                .size(24.dp)
                .weight(1f)
                .clickable{
                    navController.navigate(NavRoutes.Profile.route)
                },
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
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