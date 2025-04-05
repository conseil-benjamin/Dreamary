package com.example.dreamary.views.activities.stats

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.viewmodels.stats.StatsViewModel
import com.example.dreamary.viewmodels.stats.StatsViewModelFactory
import com.example.dreamary.views.components.BottomNavigation
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.views.components.Loading
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory (DreamRepository(LocalContext.current), AuthRepository(LocalContext.current))
    ), ) {

    val dreamsState = viewModel.dreams.collectAsState()
    val dreams = dreamsState.value
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val user by viewModel.user.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getProfileData(userId ?: "")
        viewModel.getAllDreamsForUser(userId ?: "", coroutineScope)
    }

    // TODO : faire un loader temps que les données ne sont pas récupérer et les graph définit
    Text("salut !")
    Scaffold (
        bottomBar = {
            BottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        if (user == null) {
            Loading()
            return@Scaffold
        }
        // TODO: récupérer les stats basique dispo dans le profil d'un user
        // TODO : ensuite récupérer directement tous les rêves et faire par exemple
        // TODO : une proportion des émotions, des tags, une moyenne de l'impact émotionnel et également de clareté
        LazyColumn (
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Chart(user)
            }
        }

    }
}

@Composable
fun Chart(user: User?) {
    Log.i("StatsScreen", "user: $user")
    val dreamsNormal: Double = (user?.dreamStats?.get("totalDreams")?.toDouble() ?: 0.0) - (user?.dreamStats?.get("lucidDreams")?.toDouble() ?: 0.0) - (user?.dreamStats?.get("nightmares")?.toDouble() ?: 0.0)
    var data by remember {
        mutableStateOf(
            listOf(
                Pie(label = "Cauchemar", data = user?.dreamStats?.get("nightmares")?.toDouble() ?: 0.0, color = Color(0xFFeff2fe), selectedColor = Color(0xFFeff2fe)),
                Pie(label = "Lucide", data = user?.dreamStats?.get("lucidDreams")?.toDouble() ?: 0.0, color = Color(0xFFfef9c2), selectedColor = Color(0xFFfef9c2)),
                Pie(label = "Rêve", data = dreamsNormal, color = Color(0xFFfee3e1), selectedColor = Color(0xFFfee3e1)),
            )
        )
    }
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ){
        PieChart(
            modifier = Modifier.size(200.dp),
            data = data,
            onPieClick = {
                println("${it.label} Clicked")
                val pieIndex = data.indexOf(it)
                data = data.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
            },
            selectedScale = 1.2f,
            scaleAnimEnterSpec = spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            colorAnimEnterSpec = tween(300),
            colorAnimExitSpec = tween(300),
            scaleAnimExitSpec = tween(300),
            spaceDegreeAnimExitSpec = tween(300),
            style = Pie.Style.Fill
        )
        Row (
            modifier = Modifier.padding(8.dp)
        ){
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = Color.Red,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cauchemar",
                    modifier = Modifier.padding(2.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = Color.Cyan,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "Lucide",
                    modifier = Modifier.padding(4.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        color = Color.Gray,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "Rêve",
                    modifier = Modifier.padding(4.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}