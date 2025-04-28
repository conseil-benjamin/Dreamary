package com.example.dreamary.views.activities.Guide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dreamary.views.components.BottomNavigation
import com.example.dreamary.views.components.TopNavigation

@Composable
fun GuideActivity(
    navController: NavController
) {
    var expandedSection by remember { mutableStateOf("introduction") }

    Scaffold (
        bottomBar = { BottomNavigation(navController = navController) },
        topBar = { TopNavigation(navController = navController) },
    ){
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DreamaryGuideScreen(
                expandedSection,
                setExpandedSection = { section ->
                    expandedSection = section
                }
            )
        }
    }
}

@Composable
fun DreamaryGuideScreen(
    expandedSection: String,
    setExpandedSection: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(MaterialTheme.colorScheme.surface.value)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Moon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Guide des Rêves - Dreamary",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Ce guide vous présente les bases des rêves lucides et des techniques pour améliorer " +
                                "votre expérience onirique, sans progression ni niveaux à atteindre. " +
                                "Prenez le temps d'explorer les sections qui vous intéressent.",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // Introduction Section
                item {
                    GuideSection(
                        title = "Comprendre les rêves lucides",
                        icon = Icons.Default.Star,
                        isExpanded = expandedSection == "introduction",
                        onToggle = {
                            setExpandedSection(
                                if (expandedSection == "introduction") "" else "introduction"
                            )
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle(text = "Qu'est-ce qu'un rêve lucide ?")
                            Text(
                                text = "Un rêve lucide est un rêve dans lequel vous êtes conscient que vous êtes en train de rêver. " +
                                        "Cette prise de conscience vous permet potentiellement d'observer, d'influencer ou même de " +
                                        "contrôler votre expérience onirique.",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            SectionTitle(text = "Pourquoi rechercher la lucidité dans les rêves ?")
                            BulletList(
                                items = listOf(
                                    "Explorer librement votre monde intérieur",
                                    "Surmonter des peurs et réduire les cauchemars",
                                    "Stimuler votre créativité",
                                    "Pratiquer des compétences (comme jouer d'un instrument)",
                                    "S'amuser et vivre des expériences impossibles dans la réalité"
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            SectionTitle(text = "Les phases du sommeil et les rêves")
                            Text(
                                text = "La plupart des rêves surviennent pendant la phase de sommeil paradoxal (REM - Rapid Eye Movement). " +
                                        "Un cycle de sommeil dure environ 90 minutes, et les phases REM s'allongent au fil de la nuit, " +
                                        "rendant les dernières heures de sommeil particulièrement propices aux rêves lucides.",
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Basic Techniques Section
                item {
                    GuideSection(
                        title = "Techniques pour stimuler vos rêves",
                        icon = Icons.Default.Star,
                        isExpanded = expandedSection == "basicTechniques",
                        onToggle = {
                            setExpandedSection(
                                if (expandedSection == "basicTechniques") "" else "basicTechniques"
                            )
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle(text = "Journal de rêves")
                            Text(
                                text = "La tenue d'un journal de rêves est fondamentale pour améliorer votre mémoire onirique. " +
                                        "Dès le réveil, notez tout ce dont vous vous souvenez, même les fragments. Avec le temps, " +
                                        "vous vous souviendrez de plus en plus de détails.",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SectionTitle(text = "Améliorer la qualité du sommeil")
                            Text(
                                text = "Pour favoriser des rêves plus riches et mémorables :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            BulletList(
                                items = listOf(
                                    "Maintenez un horaire de sommeil régulier",
                                    "Évitez les écrans une heure avant le coucher",
                                    "Limitez la caféine et l'alcool avant de dormir",
                                    "Assurez-vous que votre chambre est calme, sombre et fraîche",
                                    "Pratiquez une courte méditation ou relaxation avant de dormir"
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SectionTitle(text = "Suppléments naturels")
                            Text(
                                text = "Certaines substances naturelles peuvent intensifier vos rêves :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            BulletList(
                                items = listOf(
                                    "Vitamine B6 (en dose modérée)",
                                    "Thé de camomille avant le coucher",
                                    "Racine de valériane"
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Note : Consultez un professionnel de santé avant de prendre tout supplément.",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Lucid Techniques Section
                item {
                    GuideSection(
                        title = "Techniques pour devenir lucide",
                        icon = Icons.Default.Star,
                        isExpanded = expandedSection == "lucidTechniques",
                        onToggle = {
                            setExpandedSection(
                                if (expandedSection == "lucidTechniques") "" else "lucidTechniques"
                            )
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle(text = "Tests de réalité")
                            Text(
                                text = "Pratiquez régulièrement ces tests pendant la journée :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Column(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                                BulletItemWithBold(
                                    boldText = "Test de la respiration",
                                    regularText = " : Pincez-vous le nez et essayez de respirer. Dans un rêve, vous pourrez respirer malgré votre nez pincé."
                                )
                                BulletItemWithBold(
                                    boldText = "Test des mains",
                                    regularText = " : Regardez vos mains attentivement. Dans un rêve, elles peuvent apparaître floues, déformées ou avoir un nombre incorrect de doigts."
                                )
                                BulletItemWithBold(
                                    boldText = "Test du texte",
                                    regularText = " : Lisez un texte, détournez le regard, puis relisez-le. Dans un rêve, le texte change souvent."
                                )
                                BulletItemWithBold(
                                    boldText = "Test de l'interrupteur",
                                    regularText = " : Actionnez un interrupteur de lumière. Dans un rêve, la lumière change rarement comme prévu."
                                )
                            }

                            SectionTitle(text = "Technique MILD (Mnemonic Induction of Lucid Dreams)")
                            Text(
                                text = "Cette technique utilise l'autosuggestion avant de s'endormir :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            NumberedList(
                                items = listOf(
                                    "Rappelez-vous un rêve récent",
                                    "Allongé confortablement, répétez mentalement : \"La prochaine fois que je rêverai, je me souviendrai que je rêve\"",
                                    "Visualisez-vous en train de reconnaître que vous êtes dans un rêve",
                                    "Continuez jusqu'à ce que cette intention soit la dernière pensée avant de vous endormir"
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SectionTitle(text = "Technique WBTB (Wake Back To Bed)")
                            Text(
                                text = "Cette technique consiste à interrompre brièvement votre sommeil pour augmenter vos chances de lucidité :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            NumberedList(
                                items = listOf(
                                    "Réglez une alarme pour vous réveiller après 5-6 heures de sommeil",
                                    "Restez éveillé pendant 10-30 minutes (lisez sur les rêves lucides ou réfléchissez à vos rêves récents)",
                                    "Retournez vous coucher en pratiquant la technique MILD"
                                )
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Control Section
                item {
                    GuideSection(
                        title = "Stabiliser et explorer vos rêves",
                        icon = Icons.Default.Star,
                        isExpanded = expandedSection == "control",
                        onToggle = {
                            setExpandedSection(
                                if (expandedSection == "control") "" else "control"
                            )
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle(text = "Maintenir la lucidité")
                            Text(
                                text = "Une fois que vous réalisez que vous rêvez, la surexcitation peut vous réveiller ou vous faire " +
                                        "perdre la lucidité. Essayez ces techniques :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Column(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                                BulletItemWithBold(
                                    boldText = "Frottez vos mains",
                                    regularText = " : Ce mouvement physique aide à stabiliser le rêve"
                                )
                                BulletItemWithBold(
                                    boldText = "Tournez sur vous-même",
                                    regularText = " : Cela maintient votre engagement dans le rêve"
                                )
                                BulletItemWithBold(
                                    boldText = "Dites \"Clarté maintenant\"",
                                    regularText = " ou \"Ce rêve est stable\""
                                )
                                BulletItemWithBold(
                                    boldText = "Touchez des surfaces",
                                    regularText = " et concentrez-vous sur leurs textures"
                                )
                            }

                            SectionTitle(text = "Navigation dans le rêve")
                            Text(
                                text = "Pour explorer et diriger vos rêves lucides :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Column(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                                BulletItemWithBold(
                                    boldText = "Changement de scène",
                                    regularText = " : Imaginez une porte qui mène à l'endroit désiré"
                                )
                                BulletItemWithBold(
                                    boldText = "Vol",
                                    regularText = " : Commencez par sauter ou rebondir, puis laissez-vous porter"
                                )
                                BulletItemWithBold(
                                    boldText = "Invocation",
                                    regularText = " : Pour faire apparaître quelqu'un, imaginez qu'il est derrière une porte ou un coin"
                                )
                                BulletItemWithBold(
                                    boldText = "Transformation",
                                    regularText = " : Pour changer d'apparence, tournez sur vous-même tout en visualisant votre nouvelle forme"
                                )
                            }

                            SectionTitle(text = "Pratiques avancées")
                            Text(
                                text = "Une fois à l'aise avec la lucidité, vous pouvez essayer :",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            BulletList(
                                items = listOf(
                                    "Poser des questions à votre subconscient (à travers des personnages ou l'environnement du rêve)",
                                    "Résoudre des problèmes créatifs ou personnels",
                                    "Explorer des lieux impossibles ou fantastiques",
                                    "Pratiquer des compétences réelles dans un environnement sans risque"
                                )
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Troubleshooting Section
                item {
                    GuideSection(
                        title = "Résoudre les problèmes courants",
                        icon = Icons.Default.Star,
                        isExpanded = expandedSection == "troubleshooting",
                        onToggle = {
                            setExpandedSection(
                                if (expandedSection == "troubleshooting") "" else "troubleshooting"
                            )
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle(text = "Je ne me souviens pas de mes rêves")
                            Text(
                                text = "Restez immobile au réveil et essayez de vous rappeler des fragments. Tenez un journal de rêves " +
                                        "même si vous n'écrivez qu'une phrase. Réduisez le café et l'alcool qui perturbent les phases REM.",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SectionTitle(text = "Je me réveille dès que je deviens lucide")
                            Text(
                                text = "C'est courant au début. Pour éviter cela, restez calme quand vous réalisez que vous rêvez. " +
                                        "Concentrez-vous sur des sensations physiques dans le rêve comme toucher le sol ou " +
                                        "frotter vos mains pour vous ancrer dans l'expérience.",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SectionTitle(text = "Je n'arrive pas à contrôler mon rêve")
                            Text(
                                text = "Le contrôle vient avec la pratique. Commencez par des actions simples comme regarder vos mains " +
                                        "ou faire un petit saut. Utilisez des intentions plutôt que des efforts : au lieu de \"forcer\" " +
                                        "un changement, supposez qu'il va se produire naturellement.",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SectionTitle(text = "Je fais des tests de réalité, mais ils ne fonctionnent pas")
                            Text(
                                text = "Assurez-vous de faire vos tests avec une véritable curiosité, pas machinalement. " +
                                        "Remettez réellement en question votre état pendant quelques secondes. " +
                                        "Variez vos tests et pratiquez-les plusieurs fois par jour, surtout dans des situations inhabituelles.",
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun GuideSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEBF5FF))
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF1E40AF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = Color(0xFF1E3A8A),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Star else Icons.Default.Star,
                    contentDescription = if (isExpanded) "Réduire" else "Développer",
                    tint = Color(0xFF1E40AF)
                )
            }

            if (isExpanded) {
                Surface(
                    color = Color.White
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF111827),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun BulletList(items: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "• ",
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun NumberedList(items: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}. ",
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun BulletItemWithBold(boldText: String, regularText: String) {
    Row(
        modifier = Modifier.padding(bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = boldText,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = regularText,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}