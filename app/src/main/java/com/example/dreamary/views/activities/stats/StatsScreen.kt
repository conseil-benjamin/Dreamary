package com.example.dreamary.views.activities.stats

import android.util.Log
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.views.components.Loading
import com.google.firebase.auth.FirebaseAuth
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.example.dreamary.R

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

    // TODO : faire un loader tant que les données ne sont pas récupérer et les graph définit
    Text("salut !")
    Scaffold (
        bottomBar = {
            BottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        if (user == null && !dreams.isEmpty()) {
            Loading()
            return@Scaffold
        } else if (dreams.isEmpty() && user !== null && dreams != null){
            Column (
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vous n'avez pas encore de rêves !",
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }
            return@Scaffold
        }
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
                Text(
                    modifier = Modifier
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    text = "Tableau de Bord Dreamary",
                    fontStyle = MaterialTheme.typography.titleLarge.fontStyle
                )
                Text(
                    modifier = Modifier
                        .padding(8.dp),
                    text ="Analysez vos rêves et découvrez des tendances fascinantes",
                    textAlign = TextAlign.Center,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                )
            }
            item {
                StatsGenerals(
                    user,
                    dreams
                )
            }
            item {
                ComposableCharts()
            }
            item {
                PieChartStatsDreamUser(user)
            }
            item {
                LineChart()
            }
        }

    }
}

@Composable
fun PieChartStatsDreamUser(user: User?) {
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

@Composable
fun ItemCardStat(
    title: String,
    value: String,
    icon: Int,
    iconTint: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier
                    .size(48.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LineChart() {
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 22.dp),
        data = remember {
            listOf(
                Line(
                    label = "Windows",
                    values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
                    color = SolidColor(Color(0xFF23af92)),
                    firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                    secondGradientFillColor = Color.Transparent,
                    strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                    gradientAnimationDelay = 1000,
                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                )
            )
        },
//        animationMode = AnimationMode.Together(delayBuilder = {
//            it * 500L
//        }),
    )
}

@Composable
fun ComposableCharts () {
    ColumnChart(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        data = remember {
            listOf(
                Bars(
                    label = "Jan",
                    values = listOf(
                        Bars.Data(label = "Linux", value = 50.0, color = Brush.verticalGradient(...),
                        Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red))
                    ),
                ),
                Bars(
                    label = "Feb",
                    values = listOf(
                        Bars.Data(label = "Linux", value = 80.0, color = Brush.verticalGradient(...),
                        Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                    ),
                )
            )
        },
        barProperties = BarProperties(
            radius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
            spacing = 3.dp,
            strokeWidth = 20.dp
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )

}

@Composable
fun StatsGenerals(
    user: User?,
    dreams: List<Dream>?
) {
    var clarityTotal = 0
    var emotionalImpactTotal = 0
    dreams?.forEach() {
        it.characteristics.get("clarity")?.let { clarity ->
            clarityTotal += clarity
        }
        it.characteristics.get("emotionalImpact")?.let { emotionalImpact ->
            emotionalImpactTotal += emotionalImpact
        }
    }
    Log.i("clarityTotal", clarityTotal.toString())
    val totalDreams = (user?.dreamStats?.get("totalDreams") as? Number)?.toDouble() ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box {
                ItemCardStat(
                    title = "Total de rêves",
                    value = totalDreams.toInt().toString(),
                    icon = R.drawable.calendar,
                    iconTint = Color(0xFF6366F1) // Indigo
                )
            }
            Box {
                ItemCardStat(
                    title = "Rêves lucides",
                    value = "57",
                    icon = R.drawable.lune, // Remplacez par votre icône
                    iconTint = Color(0xFF9333EA) // Purple
                )
            }
            Box {
                ItemCardStat(
                    title = "Clarté moyenne",
                    value = if (clarityTotal != 0) {
                        String.format("%.1f", clarityTotal.toDouble() / totalDreams)
                    } else {
                        "50"
                    },
                    icon = R.drawable.clarte,
                    iconTint = Color(0xFFEAB308) // Yellow
                )
            }
            Box {
                ItemCardStat(
                    title = "Impact émotionel moyen",
                    value = if (emotionalImpactTotal != 0) {
                        String.format("%.1f", emotionalImpactTotal.toDouble() / totalDreams)
                    } else {
                        "50"
                    },
                    icon = R.drawable.emotion,
                    iconTint = Color(0xFF22C55E) // Green
                )
            }
        }
    }
}