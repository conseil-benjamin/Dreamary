package com.example.dreamary.views.activities.onboardingScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val features: List<String>
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Donnez vie à vos rêves",
            subtitle = "Capturez l'essence de vos aventures nocturnes",
            description = "Dreamary transforme vos rêves en histoires extraordinaires. Explorez vos souvenirs les plus précieux avec une simplicité déconcertante.",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)),
            features = listOf(
                "Journal intuitif avec texte et audio",
                "Galerie de rêves personnalisée",
                "Recherche intelligente dans vos souvenirs"
            )
        ),
        OnboardingPage(
            title = "Maîtrisez l'art du rêve lucide",
            subtitle = "Devenez le héros de vos rêves",
            description = "Découvrez des techniques éprouvées pour prendre conscience de vos rêves. Transformez votre sommeil en terrain d'exploration infini.",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF2563EB)),
            features = listOf(
                "Techniques guidées de rêve lucide",
                "Méditations personnalisées",
                "Suivi de vos progrès en temps réel"
            )
        ),
        OnboardingPage(
            title = "Une communauté qui comprend vos rêves",
            subtitle = "Partagez, interprétez, grandissez ensemble",
            description = "Rejoignez des milliers de rêveurs passionnés. Échangez vos expériences et découvrez de nouvelles perspectives sur vos aventures oniriques.",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFFA855F7), Color(0xFFEC4899)),
            features = listOf(
                "Groupes thématiques passionnants",
                "Partage sécurisé de vos rêves",
                "Interprétations collaboratives"
            )
        ),
        OnboardingPage(
            title = "Révélez les secrets de vos rêves",
            subtitle = "Une analyse profonde et personnalisée",
            description = "Découvrez les patterns cachés de vos rêves grâce à notre intelligence artificielle. Obtenez des insights uniques sur votre monde onirique.",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
            features = listOf(
                "Analyse émotionnelle avancée",
                "Détection de patterns récurrents",
                "Prédictions personnalisées"
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Contenu principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Page courante
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(pages[currentPage].gradientColors)
                    )
            ) {
                // Points de navigation
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (currentPage == index) Color.White
                                    else Color.White.copy(alpha = 0.5f)
                                )
                                .width(if (currentPage == index) 24.dp else 8.dp)
                                .height(8.dp)
                                .clickable { currentPage = index }
                        )
                    }
                }

                // Contenu de la page
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icône avec animation
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = pages[currentPage].icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = pages[currentPage].title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = pages[currentPage].subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pages[currentPage].description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Features
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp)
        ) {
            pages[currentPage].features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = pages[currentPage].gradientColors[0],
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = feature, color = Color.Gray)
                }
            }
        }

        // Boutons de navigation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp)
        ) {
            if (currentPage == pages.size - 1) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[currentPage].gradientColors[0]
                    )
                ) {
                    Text(
                        "Commencer mon voyage onirique",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { currentPage = pages.size - 1 }) {
                        Text("Passer l'introduction", color = Color.Gray)
                    }
                    Button(
                        onClick = { currentPage++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[currentPage].gradientColors[0]
                        )
                    ) {
                        Text("Suivant", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}