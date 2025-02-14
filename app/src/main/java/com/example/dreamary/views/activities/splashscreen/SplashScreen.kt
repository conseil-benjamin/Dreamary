package com.example.dreamary.views.activities.splashscreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

//@Composable
//fun SplashScreen(
//    onSplashScreenFinished: () -> Unit,
//    viewModel: SplashScreenViewModel
//) {
//    val primaryColor = Color(0xFF4F46E5)
//    val secondaryColor = Color(0xFF7C3AED)
//    val context = LocalContext.current
//
//    LaunchedEffect(Unit) {
//        if (context.getSharedPreferences("isLoggedIn", 0).getBoolean("isLoggedIn", false)) {
//            viewModel.loadData()
//        } else {
//            onSplashScreenFinished()
//        }
//        // TODO : Charger les données nécessaires pour l'application
//        // vérifier d'abord si l'utilisateur est connecté grace aux sharedpreferences
//        // s'il l'est pas on fait aucune requête
//        // sinon on appelle le viewModel qui gère l'appel pour récuper les dreams et les données user
//        // une fois les données chargées on sauvegarde les données dans les sharedPreferences et on les récupère dans le composant Home
//        // et on appelle onSplashScreenFinished() pour passer à la page suivante
//        // voir si du coup quand on revient à chaque fois sur la page d'accueil on refait une requete
//        // ou si on prend ce qu'il y a en sharedPreferences
//        // et mettre un délai de 2 secondes pour afficher le splashscreen au cas ou les données sont chargés trop rapidement
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(primaryColor, secondaryColor)
//                )
//            )
//    ) {
//        // Étoiles scintillantes
//        repeat(20) {
//            AnimatedStar(
//                x = Random.nextFloat(),
//                y = Random.nextFloat(),
//                delay = Random.nextInt(2000)
//            )
//        }
//
//        // Contenu principal
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Logo avec animation de flottement
//            Box(contentAlignment = Alignment.Center) {
//                // Halo lumineux derrière le logo
//                val glowScale by rememberInfiniteTransition().animateFloat(
//                    initialValue = 1f,
//                    targetValue = 1.2f,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(2000),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//
//                Box(
//                    modifier = Modifier
//                        .size(150.dp)
//                        .scale(glowScale)
//                        .blur(50.dp)
//                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
//                )
//
//                // Logo flottant
//                val floatOffset by rememberInfiniteTransition().animateFloat(
//                    initialValue = 0f,
//                    targetValue = 20f,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(3000, easing = FastOutSlowInEasing),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//
//                Box(
//                    modifier = Modifier
//                        .offset(y = -floatOffset.dp)
//                        .size(128.dp)
//                        .background(
//                            color = Color.White.copy(alpha = 0.1f),
//                            shape = CircleShape
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Star,
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(64.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Textes avec animation de fondu
//            var textVisible by remember { mutableStateOf(false) }
//            LaunchedEffect(Unit) {
//                textVisible = true
//            }
//
//            val textAlpha by animateFloatAsState(
//                targetValue = if (textVisible) 1f else 0f,
//                animationSpec = tween(1000)
//            )
//
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.alpha(textAlpha)
//            ) {
//                Text(
//                    text = "Dreamary",
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = "Explorez vos rêves",
//                    fontSize = 16.sp,
//                    color = Color(0xFFE0E7FF)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(64.dp))
//
//            // Points de chargement
//            LoadingDots()
//        }
//    }
//}

@Composable
fun AnimatedStar(x: Float, y: Float, delay: Int) {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(Color.White.copy(alpha = 0.6f), CircleShape)
                .align(Alignment.TopStart)
                .offset(
                    x = (x * 100).dp,
                    y = (y * 100).dp
                )
        )
    }
}

@Composable
fun LoadingDots() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        repeat(3) { index ->
            val alpha by rememberInfiniteTransition().animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(Color.White, CircleShape)
            )
        }
    }
}