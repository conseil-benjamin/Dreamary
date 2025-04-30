package com.example.dreamary.views.activities.onboardingScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.dreamary.R

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: Int,
    val iconFeatures: Int,
    val gradientColors: List<Color>,
    val features: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Donnez vie à vos rêves",
            subtitle = "Capturez l'essence de vos aventures nocturnes",
            description = "Dreamary transforme vos rêves en histoires extraordinaires. Explorez vos souvenirs les plus précieux avec une simplicité déconcertante.",
            icon = R.drawable.calendar,
            iconFeatures = R.drawable.dot,
            gradientColors = listOf(
                Color(0xFF6A517B), // PrimaryVariant — violet profond
                Color(0xFF2A2438)  // DarkBackground — violet très foncé (fonctionne même en light mode)
            ),
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
            icon = R.drawable.lune,
            iconFeatures = R.drawable.dot,
            gradientColors = listOf(
                Color(0xFF6A517B), // PrimaryVariant — violet profond
                Color(0xFF2A2438)  // DarkBackground — violet très foncé (fonctionne même en light mode)
            ),
            features = listOf(
                "Techniques guidées de rêve lucide",
                "Méditations personnalisées",
            )
        ),
        OnboardingPage(
            title = "Une communauté qui comprend",
            subtitle = "Partagez, interprétez, grandissez ensemble",
            description = "Rejoignez des rêveurs passionnés. Échangez vos expériences et découvrez de nouvelles perspectives sur vos aventures oniriques.",
            icon = R.drawable.users,
            iconFeatures = R.drawable.dot,
            gradientColors = listOf(
                Color(0xFF6A517B), // PrimaryVariant — violet profond
                Color(0xFF2A2438)  // DarkBackground — violet très foncé (fonctionne même en light mode)
            ),
            features = listOf(
                "Groupes thématiques passionnants",
                "Partage sécurisé de vos rêves",
                "Interprétations collaboratives"
            )
        ),
        OnboardingPage(
            title = "Révélez les secrets",
            subtitle = "Une analyse profonde et personnalisée",
            description = "Plongez dans vos rêves avec des outils d'analyse avancés. Découvrez des tendances, des émotions et des insights uniques pour mieux comprendre votre monde onirique.",
            icon = R.drawable.search,
            iconFeatures = R.drawable.dot,
            gradientColors = listOf(
                Color(0xFF6A517B), // PrimaryVariant — violet profond
                Color(0xFF2A2438)  // DarkBackground — violet très foncé (fonctionne même en light mode)
            ),
            features = listOf(
                "Analyse émotionnelle avancée",
                "Détection de patterns récurrents",
                "Prédictions personnalisées"
            )
        ),
        OnboardingPage(
            title = "Fonctionnalités Premium",
            subtitle = "Débloquez le plein potentiel de vos rêves",
            description = "Accédez à des outils avancés et à des analyses approfondies pour maximiser votre expérience de rêve.",
            icon = R.drawable.premium,
            iconFeatures = R.drawable.dot,
            gradientColors = listOf(
                Color(0xFF6A517B), // PrimaryVariant — violet profond
                Color(0xFF2A2438)  // DarkBackground — violet très foncé (fonctionne même en light mode)
            ),
            features = listOf(
                "Génération d'images IA pour vos rêves",
                "Analyse avancée",
                "Collections personnalisées",
            )
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFDFBFA), Color(0xFFEFE9F4))
                )
            )
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
                // Optionnel : halo lumineux en fond
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.4f }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                                radius = 500f
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .padding(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = pages[page].icon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
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

                    Spacer(modifier = Modifier.height(15.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pages[page].features.forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = pages[page].iconFeatures),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = feature,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
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
                .padding(bottom = 100.dp, top = 25.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                )

                val dotSize by animateDpAsState(
                    targetValue = if (isSelected) 16.dp else 8.dp
                )

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .shadow(if (isSelected) 6.dp else 0.dp, CircleShape)
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