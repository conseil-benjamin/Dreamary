package com.example.dreamary.views.activities.Premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dreamary.R

@Composable
fun PremiumPresentation(
    navController: NavController,
) {
    var selectedPlan by remember { mutableStateOf("yearly") }

    val plans = mapOf(
        "monthly" to Plan(price = "1.99", period = "mois"),
        "yearly" to Plan(price = "14.99", period = "an"),
        "lifetime" to Plan(price = "49.99", isOnce = true)
    )

    val features = listOf(
        Feature(
            icon = { Icon(painter = painterResource(id = R.drawable.generation_image_ai), contentDescription = null) },
            title = "Génération d'images AI",
            description = "Visualisez vos rêves"
        ),
        Feature(
            icon = {Icon(painter = painterResource(id = R.drawable.advanced_analys), contentDescription = null) },
            title = "Analyse avancée",
            description = "Patterns et prédictions"
        ),
        Feature(
            icon = { Icon(painter = painterResource(id = R.drawable.collections), contentDescription = null) },
            title = "Collections personnalisées",
            description = "Organisez vos rêves par thème"
        ),
//        Feature(
//            icon = { Icon(painter = painterResource(id = R.drawable.generation_image_ai), contentDescription = null) },  // Utilisé en remplacement d'Infinity
//            title = "Audio illimité",
//            description = "Enregistrez tous vos rêves"
//        ),
        Feature(
            icon = { Icon(painter = painterResource(id = R.drawable.generation_image_ai), contentDescription = null) },
            title = "Interprétations de vos rêves via l'IA",
            description = "Comprenez vos rêves"
        )
    )

    val bonuses = listOf(
        "Cours de rêve lucide",
        "Méditations guidées",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5),
                                    Color(0xFF9333EA)
                                )
                            )
                        )
                        .padding(top = 16.dp, bottom = 32.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Dreamary Premium",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Débloquez tout le potentiel de vos rêves",
                                color = Color(0xFFE0E7FF),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Plans Selection
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            plans.forEach { (planKey, plan) ->
                                PlanOption(
                                    plan = plan,
                                    isSelected = selectedPlan == planKey,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedPlan = planKey }
                                )
                            }
                        }
                    }
                }
            }

            // Features
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Fonctionnalités Premium",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    features.forEach { feature ->
                        FeatureCard(feature = feature)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

//            // Additional Benefits
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                ) {
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = CardDefaults.cardColors(
//                            containerColor = Color(0xFFFEF9E7)
//                        )
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Star,
//                                    contentDescription = null,
//                                    tint = Color(0xFFCA8A04),
//                                    modifier = Modifier.size(20.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Bonus Premium",
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color(0xFF78350F)
//                                )
//                            }

//                            bonuses.forEach { bonus ->
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier.padding(vertical = 4.dp)
//                                ) {
//                                    Box(
//                                        modifier = Modifier
//                                            .size(4.dp)
//                                            .background(Color(0xFFCA8A04), CircleShape)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(
//                                        text = bonus,
//                                        fontSize = 14.sp,
//                                        color = Color(0xFF78350F)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            // Spacer to push CTA to bottom when content is short
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // CTA at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { /* Traitement de l'abonnement */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.premium),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Bientot disponible", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
//                Text(
//                    text = "7 jours d'essai gratuit",
//                    fontSize = 14.sp,
//                    color = Color.Gray
//                )
            }
        }
    }
}

@Composable
fun PlanOption(
    plan: Plan,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .background(
                color = if (isSelected) Color(0xFF4F46E5) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${plan.price}€",
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF4B5563)
            )
            Text(
                text = if (plan.isOnce) "À vie" else "/${plan.period}",
                fontSize = 12.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.75f) else Color(0xFF4B5563).copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun FeatureCard(feature: Feature) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides Color(0xFF4F46E5)) {
                    feature.icon()
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = feature.title,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
                Text(
                    text = feature.description,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

data class Plan(
    val price: String,
    val period: String = "",
    val isOnce: Boolean = false
)

data class Feature(
    val icon: @Composable () -> Unit,
    val title: String,
    val description: String
)