package com.example.dreamary.views.activities.onboardingScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val features: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    // todo : ajouter une page pour présenter le mode premium et ses avantages
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
            title = "Une communauté qui comprend",
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
            title = "Révélez les secrets",
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

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(pages[page].gradientColors))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

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
                            imageVector = pages[page].icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = pages[page].title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pages[page].subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = pages[page].description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        pages[page].features.forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = feature,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Points de navigation
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val width by animateDpAsState(
                    targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                )

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .width(width)
                        .height(8.dp)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(iteration)
                            }
                        }
                )
            }
        }

        // Navigation buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (pagerState.currentPage == pages.lastIndex) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].gradientColors[0]
                    )
                ) {
                    Text(
                        "Commencer l'aventure",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Text("Précédent", color = Color.Gray)
                        }
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[pagerState.currentPage].gradientColors[0]
                        )
                    ) {
                        Text("Suivant")
                    }
                }
            }
        }
    }
}