package com.example.dreamary.views.activities.Dreams

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dreamary.ui.theme.DreamaryTheme
import com.example.dreamary.R
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.viewmodels.dreams.DetailsDreamViewModel
import com.example.dreamary.viewmodels.dreams.DetailsDreamViewModelFactory
import com.example.dreamary.views.components.Loading
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModel
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsDreamActivity(
    navController: NavController,
    dreamId: String,
    viewModel: DetailsDreamViewModel = viewModel(
        factory = DetailsDreamViewModelFactory (DreamRepository(LocalContext.current))
    ),
    ) {
    val dream = viewModel.dream.collectAsState(initial = Dream()).value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(dreamId) {
        viewModel.getDreamById(dreamId)
    }

    DreamaryTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.back),
                                contentDescription = "Retour",
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Share */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.share),
                                contentDescription = "Partager",
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* TODO: Edit */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit),
                                contentDescription = "Modifier",
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            if (dream == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chargement...")
                    Loading()
                }
                return@Scaffold
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    HeaderDream(dream = dream)
                }

                item {
                    DreamContent(dream = dream)
                }

                item {
                    AudioPlayerDream(dream = dream)
                }

                item {
                    Emotions(dream = dream)
                }

                item {
                    Tags(dream = dream)
                }

                item {
                  ContextSleepDream(dream = dream)
                }

                item {
                    CharacteristicsDream(dream = dream)
                }

                item {
                   Environment(dream = dream)
                }

                item {
                    AnalyseDream(dream = dream)
                }
            }
        }
    }
}

@Composable
fun DreamContent(dream: Dream) {
    // Titre et contenu
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = dream?.content ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun HeaderDream(dream: Dream) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                when (dream?.dreamType) {
                    "Rêve" -> Color(0xFFeff2fe)
                    "Lucide" -> Color(0xFFfef9c2)
                    "Cauchemar" -> Color(0xFFfee3e1)
                    else -> Color(0xFFeff2fe)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(
                    id = when (dream.dreamType) {
                        "Rêve" -> R.drawable.lune
                        "Lucide" -> R.drawable.etoile
                        "Cauchemar" -> R.drawable.cauchemar
                        else -> R.drawable.lune
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = when (dream.dreamType) {
                    "Rêve" -> Color(0xFF555393)
                    "Lucide" -> Color(0xFF8c4f54)
                    "Cauchemar" -> Color(0xFF8f7036)
                    else -> Color(0xFF555393)
                }
            )
            Text(
                text = dream.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = dream.createdAt.toDate().format(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AudioPlayerDream(
    dream: Dream,
    viewModel: AudioRecorderViewModel = viewModel(
        factory = AudioRecorderViewModelFactory(LocalContext.current)
    )
) {
    val isPlaying by viewModel.isPlaying.collectAsState(initial = false)
    var isListening by remember { mutableStateOf(false) }

    // todo : faire en sorte de savoir quand l'audio est en cours de lecture ou non

    // Audio player si disponible
    dream.audio.get("path")?.let { audioPath ->
        if (audioPath.toString().isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = if(!isPlaying) R.drawable.play else R.drawable.pause),
                        contentDescription = "Lecture audio",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                // todo : la mise en pause de l'audio ne marche pas
                                if (!isPlaying && !isListening) {
                                    isListening = true
                                    viewModel.playAudioFromFirebase(dream.audio["url"].toString())
                                } else if (isPlaying) {
                                    isListening = false
                                    viewModel.pauseRecording()
                                } else {
                                    isListening = true
                                    viewModel.resumeRecording()
                                }
                            },
                    )
                    if (!isPlaying) {
                        Text(
                            text = "Écouter l'enregistrement",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        // todo : nul à chier l'image la changer
                        AsyncImage(
                            model = R.drawable.sound_wave,
                            contentDescription = "Audio",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp)
                        )
                    }
                    Text(
                        text = "${dream?.audio?.get("duration")}s",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyseDream(dream: Dream) {
    // Analyse
    SectionTitle("Analyse", R.drawable.brain)
    if (dream.analysis.isEmpty()) {
        Text(
            text = "Aucune analyse",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = dream?.analysis ?: "",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ContextSleepDream(dream: Dream) {
    SectionTitle("Contexte de sommeil", R.drawable.context)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            dream?.sleepContext?.let { context ->
                DetailRow(
                    icon = R.drawable.lhorloge,
                    label = "Heure de coucher",
                    value = if (context["time"] == "") "Non renseigné" else context["time"].toString()
                )
                DetailRow(
                    icon = R.drawable.thermometre,
                    label = "Température",
                    value = if (context["temperature"] == 0) "Non renseigné" else context["temperature"].toString()
                )
                DetailRow(
                    icon = R.drawable.bed,
                    label = "Réveils",
                    value = if (context["nbReveils"] == "") "Non renseigné" else context["nbReveils"].toString()
                )
                DetailRow(
                    icon = R.drawable.son,
                    label = "Niveau sonore",
                    value = if (context["noiseLevel"] == "") "Non renseigné" else context["noiseLevel"].toString()
                )
            }
        }
    }
}

@Composable
fun CharacteristicsDream(dream: Dream) {
    // Caractéristiques
    SectionTitle("Caractéristiques", R.drawable.features)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            dream.characteristics.let { characteristics ->
                Log.d("CharacteristicsDream", characteristics.toString())
                Log.d("CharacteristicsDream", characteristics["clarity"].toString())
                Column {
                    Text("Clarté du souvenir")
                    LinearProgressIndicator(
                        progress = (characteristics["clarity"] as? Int)?.toFloat()?.div(5f) ?: 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                Column {
                    Text("Impact émotionnel")
                    LinearProgressIndicator(
                        progress = (characteristics["emotionalImpact"] as? Int)?.toFloat()?.div(5f) ?: 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Environment(dream: Dream) {
    SectionTitle("Environnement", R.drawable.environnement)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            dream.environment.let { environment ->
                DetailRow(
                    icon = R.drawable.security,
                    label = "Type",
                    value = if (environment["type"] == "Non renseigné") "Non renseigné" else environment["type"].toString()
                )
                DetailRow(
                    icon = R.drawable.season,
                    label = "Saison",
                    value = if (environment["season"] == "Non renseigné") "Non renseigné" else environment["season"].toString()
                )
                DetailRow(
                    icon = R.drawable.cloudy,
                    label = "Météo",
                    value = if (environment["weather"] == "Non renseigné") "Non renseigné" else environment["weather"].toString()
                )
                DetailRow(
                    icon = R.drawable.lune,
                    label = "Couleurs dominantes",
                    value = if (environment["dominantColor"] == "Non renseigné") "Non renseigné" else environment["dominantColor"].toString()
                )
            }
        }
    }
}

@Composable
fun Tags(dream: Dream){
    SectionTitle("Tags", R.drawable.tags)
    if (dream.tags.isEmpty()) {
        Text(
            text = "Aucun tag",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        return
    }
    dream.tags.forEach { (category, tags) ->
        if (tags.isNotEmpty()) {
            Text(
                text = category.replaceFirstChar { it.uppercaseChar() },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { },
                        label = { Text(tag) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }

}

@Composable
fun Emotions(dream: Dream){
    // Émotions
    SectionTitle("Émotions", R.drawable.emotions)
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        dream.emotions.forEach { emotion ->
            AssistChip(
                onClick = { },
                label = { Text(emotion) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (emotion) {
                        "Peur" -> Color(0xFFfee3e1)
                        "Joie" -> Color(0xFFfef9c2)
                        "Confusion" -> Color(0xFFf4e8ff)
                        "Paix" -> Color(0xFFdcfce7)
                        "Excitation" -> Color(0xFFffeed5)
                        "Mélancolie" -> Color(0xFFdceaff)
                        else -> MaterialTheme.colorScheme.surface
                    },
                )
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, icon: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun DetailRow(
    icon: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun Date.format(): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return formatter.format(this)
}

private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}