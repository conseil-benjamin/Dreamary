package com.example.dreamary.views.activities.Dreams

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.example.dreamary.R
import com.example.dreamary.ui.theme.DreamaryTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.viewmodels.dreams.AddDreamViewModel
import com.example.dreamary.viewmodels.dreams.AddDreamViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.Manifest
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModel
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModelFactory
import com.example.dreamary.views.components.CustomDropdown
import java.util.Calendar
import androidx.compose.ui.text.input.ImeAction
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.views.components.Loading
import kotlin.math.abs

@Preview(showBackground = true)
@Composable
fun AddDreamActivityPreview() {
    AddDreamActivity(navController = NavController(LocalContext.current))
}


@Composable
private fun ShowPermissionDialog(
    context: Context,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Microphone") },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Demander la permission")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String,
    title: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss ) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun AddDreamActivity (navController: NavController, viewModel: AddDreamViewModel = viewModel(
    factory = AddDreamViewModelFactory (DreamRepository(LocalContext.current))
)) {
    var dreamTypeChoose by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("")}
    var title by remember { mutableStateOf("")}
    val currentUser = FirebaseAuth.getInstance().currentUser
    val pickedEmotions = remember { mutableStateListOf<String>() }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showConfirmLeaveActivity by remember { mutableStateOf(false) }

    BackHandler {
        showConfirmLeaveActivity = true
    }

    var dream by remember { mutableStateOf(Dream(
        title = title,
        content = content,
        dreamType = dreamTypeChoose,
        lucid = dreamTypeChoose == "Lucide",
        isShared = false,
        analysis = "",
        emotions = pickedEmotions,
        userId = currentUser?.uid ?: "",
        createdAt = Timestamp.now(),
        audio = mutableMapOf(
            "fileName" to "",
            "duration" to 0,
            "mimeType" to "",
            "path" to "",
            "size" to 0L,
            "url" to "",
            "createdAt" to Timestamp.now()
        ),
        characteristics = mapOf(
            "clarity" to 0,
            "emotionalImpact" to 0,
            "perspective" to "",
            "timePeriod" to ""
        ),
        environment = mapOf(
            "dominantColors" to "",
            "season" to "",
            "type" to "",
            "weather" to ""
        ),
        metadata = mapOf(
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        ),
        sleepContext = mapOf(
            "noiseLevel" to "",
            "nbReveils" to "",
            "temperature" to 0,
            "time" to "",
            "quality" to 0,
            "duration" to 0f
        ),
        social = mapOf(
            "likes" to 0,
            "comments" to 0
        ),
        tags = mapOf(
            "symbols" to listOf<String>(),
            "themes" to listOf<String>(),
            "characters" to listOf<String>(),
            "places" to listOf<String>(),
            "divers" to listOf<String>()
        )
    ))
    }

    LaunchedEffect(dreamTypeChoose) {
        Log.d("dreamTypeChoose", dreamTypeChoose)
        dream = dream.copy(
            dreamType = dreamTypeChoose
        )
    }

    /**
      Mise à jour des tags
     */

    var tagsCharacters by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var tagsPlaces by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var tagsThemes by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var tagsSymbols by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var tagsDivers by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    // Mettre à jour dream.tags à chaque fois que les tags changent
    LaunchedEffect(tagsCharacters, tagsPlaces, tagsThemes, tagsSymbols) {
        Log.d("Tags2", "Updating dream.tags")
        Log.d("Tags2", tagsPlaces.toString())
        Log.d("dreamCopyTAGS", dream.toString())
        dream = dream.copy(
            tags = mapOf(
                "characters" to tagsCharacters,
                "places" to tagsPlaces,
                "themes" to tagsThemes,
                "symbols" to tagsSymbols,
                "divers" to tagsDivers
            )
        )
        Log.d("dreamCopyTAGS", dream.toString())
    }

    LaunchedEffect(title, content) {
        dream = dream.copy(title = title, content = content)
    }

    var analysisText by remember { mutableStateOf("") }

    LaunchedEffect(analysisText) {
        dream = dream.copy(analysis = analysisText)
    }

    // TODO : mise à jour des tags à chaque fois qu'on en ajoute un à la base de données

    var tagsPersonnes = listOf("Famille", "Amis", "Inconnus", "Célébrités", "Amie", "Enfant", "Conjoint", "Collègue", "Voisin", "Inconnu")
    var tagsLieux = listOf("Maison", "Travail", "École", "Nature", "Ville", "Campagne", "Mer", "Montagne", "Forêt", "Rue", "Chambre", "Cuisine", "Salle de bain", "Salon", "Jardin", "Parc", "Plage")
    var tagsActions = listOf("Courir", "Marcher", "Voler", "Nager", "Manger", "Boire", "Dormir", "Parler", "Écouter", "Regarder", "Lire", "Écrire", "Travailler", "Étudier", "Jouer", "Danser", "Chanter", "Rire", "Pleurer", "Aider", "Sauver", "Tuer", "Blesser", "Aimer", "Détester", "Crier", "Prier", "Méditer", "Rêver", "Voyager", "Conduire")
    var tagsSymboles = listOf("Lumière", "Obscurité", "Feu", "Eau", "Terre", "Air", "Ciel", "Enfer", "Paradis", "Mort", "Vie", "Naissance", "Famille", "Amour", "Haine", "Paix", "Guerre", "Richesse", "Pauvreté", "Santé", "Maladie", "Bonheur", "Tristesse", "Joie", "Peur", "Colère")

// Liste des tags customs ajoutés par l'utilisateur
    var customTagsPersonnes by remember { mutableStateOf<List<String>>(emptyList()) }
    var customTagsLieux by remember { mutableStateOf<List<String>>(emptyList()) }
    var customTagsActions by remember { mutableStateOf<List<String>>(emptyList()) }
    var customTagsSymboles by remember { mutableStateOf<List<String>>(emptyList()) }

// Fusion des tags par défaut et des tags customs
    val allPersonnesTags = remember { mutableStateOf(tagsPersonnes + customTagsPersonnes) }
    val allLieuxTags = remember { mutableStateOf(tagsLieux + customTagsLieux) }
    val allActionsTags = remember { mutableStateOf(tagsActions + customTagsActions) }
    val allSymbolesTags = remember { mutableStateOf(tagsSymboles + customTagsSymboles) }

// Liste des tags sélectionnés pour le rêve
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }

// Stocke la couleur attribuée à chaque tag sélectionné
    var selectedTagColors by remember { mutableStateOf(mutableMapOf<String, Pair<Color, Color>>()) }

    LaunchedEffect(selectedTags) {
        Log.d("SelectedTags", selectedTags.toString())

        val updatedTagsCharacters = selectedTags.filter { it in allPersonnesTags.value }
        val updatedTagsPlaces = selectedTags.filter { it in allLieuxTags.value }
        val updatedTagsThemes = selectedTags.filter { it in allActionsTags.value }
        val updatedTagsSymbols = selectedTags.filter { it in allSymbolesTags.value }
        val updatedTagsDivers = selectedTags.filter {
            it !in updatedTagsCharacters && it !in updatedTagsPlaces && it !in updatedTagsThemes && it !in updatedTagsSymbols
        }

        dream = dream.copy(
            tags = mapOf(
                "characters" to updatedTagsCharacters,
                "places" to updatedTagsPlaces,
                "themes" to updatedTagsThemes,
                "symbols" to updatedTagsSymbols,
                "divers" to updatedTagsDivers
            )
        )
        Log.d("UpdatedDream", dream.toString())
    }

    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("tags", Context.MODE_PRIVATE)
        val storedSet = sharedPrefs.getStringSet(currentUser?.uid, setOf()) ?: setOf()

        val categorizedTags = storedSet.map { tagString ->
            val parts = tagString.split(",") // Supposons que les tags sont stockés sous forme "Nom,Catégorie"
            Tag(
                name = parts.getOrNull(0) ?: "",
                category = parts.getOrNull(1) ?: "Divers" // Catégorie par défaut
            )
        }

        // Répartir les tags dans les bonnes listes
        customTagsPersonnes = categorizedTags.filter { it.category == "Personnes" }.map { it.name }
        customTagsLieux = categorizedTags.filter { it.category == "Lieux" }.map { it.name }
        customTagsActions = categorizedTags.filter { it.category == "Actions" }.map { it.name }
        customTagsSymboles = categorizedTags.filter { it.category == "Symboles" }.map { it.name }

        // Fusionner avec les tags par défaut
        allPersonnesTags.value = tagsPersonnes + customTagsPersonnes
        allLieuxTags.value = tagsLieux + customTagsLieux
        allActionsTags.value = tagsActions + customTagsActions
        allSymbolesTags.value = tagsSymboles + customTagsSymboles
    }

    var noiseLevel by remember {
        mutableStateOf("Non renseigné")
    }
    var nbReveils by remember {
        mutableStateOf("Non renseigné")
    }
    var temperature by remember {
        mutableIntStateOf(0)
    }
    var time by remember {
        mutableStateOf("")
    }

    var path by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(0) }

    LaunchedEffect(path) {
        if (path.isNotEmpty()) {
            dream = dream.copy(
                audio = dream.audio.toMutableMap().apply {
                    this["path"] = path
                    this["duration"] = duration
                    this["url"] = ""
                }
            )
        }
        Log.d("dreamCopyAUDIO", dream.toString())
    }

    var clarity by remember { mutableStateOf(0) }
    var emotionalImpact by remember { mutableStateOf(0) }

    LaunchedEffect(clarity, emotionalImpact) {
        dream = dream.copy(
            characteristics = dream.characteristics.toMutableMap().apply {
                this["clarity"] = clarity
                this["emotionalImpact"] = emotionalImpact
            }
            )
    }

    LaunchedEffect(noiseLevel, nbReveils, temperature, time) {
        Log.d("dreamCopyCONTEXT", dream.toString())
        dream = dream.copy(
            sleepContext = dream.sleepContext.toMutableMap().apply {
                this["noiseLevel"] = noiseLevel
                this["nbReveils"] = nbReveils
                this["temperature"] = temperature
                this["time"] = time
                this["quality"] = 0
                this["duration"] = 0f
            },
            audio = dream.audio // Conserve l’audio tel quel
        )
        Log.d("dreamCopyCONTEXT", dream.toString())
    }

    var selectedType by remember {
        mutableStateOf("Non renseigné")
    }
    var selectedSeason by remember {
        mutableStateOf("Non renseigné")
    }
    var selectedWeather by remember {
        mutableStateOf("Non renseigné")
    }
    var selectedColors by remember {
        mutableStateOf("Non renseigné")
    }

    LaunchedEffect(selectedType, selectedSeason, selectedWeather, selectedColors) {
        Log.d("Environment", "Type: $selectedType, Season: $selectedSeason, Weather: $selectedWeather, Colors: $selectedColors")
        Log.d("dreamCopyENVIRONMENT", dream.toString())
        dream = dream.copy(
            environment = mapOf(
                "dominantColors" to selectedColors,
                "season" to selectedSeason,
                "type" to selectedType,
                "weather" to selectedWeather
            )
        )
        Log.d("dreamCopyENVIRONMENT", dream.toString())
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showOverlay by remember { mutableStateOf(false) }

    // Ecoute des messages du SnackbarManager
    LaunchedEffect(Unit) { // unit veut dire que l'effet sera lancé une seule fois
        SnackbarManager.snackbarMessages.collect { snackbarMessage ->
            snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = snackbarMessage.actionLabel
            )
        }
    }

    val hasAudioPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //
        } else {
            // Permission refusée - afficher un message
            coroutineScope.launch {
                SnackbarManager.showMessage("L'enregistrement audio nécessite la permission du microphone", R.drawable.invite_people)
            }
        }
    }

    if (showPermissionDialog) {
        ShowPermissionDialog(
            context = context,
            onConfirm = {
                showPermissionDialog = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = {
                showPermissionDialog = false
            },
            text = "Cette application a besoin de la permission d'accès au microphone et de stockage pour enregistrer l'audio."
        )
    }


    if (showConfirmLeaveActivity) {
        ConfirmDialog(
            onConfirm = {
                showConfirmLeaveActivity = false
                navController.popBackStack()
            },
            onDismiss = {
                showConfirmLeaveActivity = false
            },
            text = "Êtes-vous sûr de vouloir quitter sans enregistrer ?",
            title = "Quitter sans enregistrer"
        )
    }


    fun checkAudioPermission(): () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasAudioPermission.value = true
            }

            (context as? Activity)?.shouldShowRequestPermissionRationale(
                Manifest.permission.RECORD_AUDIO
            ) == true -> {
                showPermissionDialog = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val savingInProgress = remember { mutableStateOf(false) }

    DreamaryTheme {
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    Topbar(navController, viewModel, coroutineScope, dream, savingInProgress, dreamTypeChoose)
                }
            ) { paddingValues ->
                if (savingInProgress.value) {
                    Loading()
                    return@Scaffold
                }
                LazyColumn(
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = !showOverlay
                ) {
                    item {
                        ContextSleep(
                            noiseLevel = noiseLevel,
                            nbReveils = nbReveils,
                            temperature = temperature,
                            time = time,
                            onNoiseLevelChanged = { noiseLevel = it },
                            onNbReveilsChanged = { nbReveils = it },
                            onTemperatureChanged = { temperature = it },
                            onTimeChanged = { time = it },
                            contextSleep = dream.sleepContext as MutableMap<String, Any>,
                            onContextSleepChanged = { dream.sleepContext = it }
                        )
                    }

                    item {
                        DreamType(
                            dreamTypeChoose = dreamTypeChoose,
                            onDreamTypeChanged = { dreamTypeChoose = it }
                        )
                    }

                    item {
                        DescribeDream(
                            title = title,
                            content = content,
                            onValueChangeTitle = { title = it },
                            onValueChangeContent = { content = it },
                            hasAudioPermission = hasAudioPermission,
                            checkAudioPermission = checkAudioPermission(),
                            onChangeShowOverlay = { showOverlay = it }
                        )
                    }

                    item {
                        Emotions(
                            pickedEmotions = pickedEmotions
                        )
                    }

                    item {
                        Tags(
                            allPersonnesTags = allPersonnesTags.value,
                            allLieuxTags = allLieuxTags.value,
                            allActionsTags = allActionsTags.value,
                            allSymbolesTags = allSymbolesTags.value,
                            selectedTags = selectedTags,
                            selectedTagsColors = selectedTagColors,
                            onTagSelected = { tag ->
                                if (tag in selectedTags) {
                                    // Si le tag est déjà sélectionné, on le retire et on supprime sa couleur associée
                                    selectedTags = selectedTags - tag
                                    selectedTagColors = selectedTagColors.toMutableMap().apply { remove(tag) }
                                } else {
                                    // Si c'est une nouvelle sélection, on attribue une couleur aléatoire
                                    val randomIndex = (colorsBackground.indices).random()
                                    val backgroundColor = colorsBackground[randomIndex]
                                    val contentColor = colorsContent[randomIndex]

                                    selectedTags = selectedTags + tag
                                    selectedTagColors = selectedTagColors.toMutableMap().apply {
                                        put(tag, Pair(backgroundColor, contentColor))
                                    }
                                }
                            },
                            onCustomTagAdded = { tag, category ->
                                when (category) {
                                    "Personnes" -> {
                                        customTagsPersonnes = customTagsPersonnes + tag
                                        allPersonnesTags.value = tagsPersonnes + customTagsPersonnes
                                    }
                                    "Lieux" -> {
                                        customTagsLieux = customTagsLieux + tag
                                        allLieuxTags.value = tagsLieux + customTagsLieux
                                    }
                                    "Actions" -> {
                                        customTagsActions = customTagsActions + tag
                                        allActionsTags.value = tagsActions + customTagsActions
                                    }
                                    "Symboles" -> {
                                        customTagsSymboles = customTagsSymboles + tag
                                        allSymbolesTags.value = tagsSymboles + customTagsSymboles
                                    }
                                }

                                // Sauvegarde dans SharedPreferences
                                val sharedPrefs = context.getSharedPreferences("tags", Context.MODE_PRIVATE)
                                val storedSet = sharedPrefs.getStringSet(currentUser?.uid, setOf()) ?: setOf()
                                val updatedTags = (storedSet + "$tag,$category").toSet()
                                sharedPrefs.edit().putStringSet(currentUser?.uid, updatedTags).apply()
                                viewModel.addTag(
                                    Tag(
                                        name = tag,
                                        category = category,
                                        isCustom = true,
                                        userId = currentUser?.uid ?: ""
                                    ),
                                    coroutineScope = coroutineScope,
                                    context = context
                                )
                            }
                        )
                    }

                    item {
                        Features(
                            clarity = clarity,
                            onClarityChanged = {clarity = it},
                            emotionalImpact = emotionalImpact,
                            onEmotionalImpactChanged = {emotionalImpact = it}
                        )
                    }
                    item {
                        Environment(
                            selectedType = selectedType,
                            selectedSeason = selectedSeason,
                            selectedWeather = selectedWeather,
                            selectedColors = selectedColors,
                            onTypeChanged = { selectedType = it },
                            onSeasonChanged = { selectedSeason = it },
                            onWeatherChanged = { selectedWeather = it },
                            onColorsChanged = { selectedColors = it },
                            environment = dream.environment as MutableMap<String, Any>,
                            onEnvironmentChanged = { dream.environment = it }
                        )
                    }
                    item {
                        AutoAnalyse(
                            analysisText = analysisText,
                            onTextChange = { analysisText = it }
                        )
                    }
                    item {
                        Share()
                    }
                }
            }
            if (showOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        .zIndex(9f)
                        .pointerInput(Unit){
                            detectTapGestures { }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(10f)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        OverlayAudioPlayer(
                            viewModel = viewModel(
                                factory = AudioRecorderViewModelFactory(LocalContext.current)
                            ),
                            onChangeShowOverlay = { showOverlay = it },
                            audio = dream.audio as MutableMap<String, Any>,
                            onPathChanged = { path = it },
                            onAudioChanged = { dream.audio = it },
                            OnDurationChanged = { duration = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Topbar (navController: NavController, viewModel: AddDreamViewModel, coroutineScope: CoroutineScope, dream: Dream, savingInProgress: MutableState<Boolean>, dreamTypeChoose: String) {
    var showConfirmLeave by remember { mutableStateOf(false) }
    dream.lucid = dreamTypeChoose == "Lucide"

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            "Annuler",
            modifier = Modifier
                .weight(1f)
                .clickable {
                    showConfirmLeave = true
                }
        )
        Button(
            onClick = {
                Log.d("test1232", dream.toString())
                savingInProgress.value = true
                viewModel.addDream(
                    navController = navController,
                    dream = dream,
                    coroutineScope = coroutineScope,
                    onSaved = {
                        savingInProgress.value = false
                        navController.navigate(NavRoutes.SucessAddDream.route)
                    },
                    onFailure = {
                        savingInProgress.value = false
                    }
                )
            }
        ) {
            Text("Enregistrer")
        }
    }

    if (showConfirmLeave) {
        ConfirmDialog(
            onConfirm = {
                showConfirmLeave = false
                navController.popBackStack()
            },
            onDismiss = {
                showConfirmLeave = false
            },
            text = "Êtes-vous sûr de vouloir annuler l'enregistrement ? Cela effacera les données saisies.",
            title = "Quitter sans enregistrer"
        )
    }
}

fun showTimePicker(context: Context, onTimeSelected: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(context, { _, selectedHour, selectedMinute ->
        onTimeSelected(selectedHour, selectedMinute)
    }, hour, minute, true)

    timePickerDialog.show()
}

@Composable
fun ContextSleep (
    noiseLevel : String,
    nbReveils : String,
    temperature : Int,
    time : String,
    onNoiseLevelChanged: (String) -> Unit,
    onNbReveilsChanged: (String) -> Unit,
    onTemperatureChanged: (Int) -> Unit,
    onTimeChanged: (String) -> Unit,
    contextSleep: MutableMap<String, Any>,
    onContextSleepChanged: (MutableMap<String, Any>) -> Unit
) {

    val context = LocalContext.current

    TitleSection(
        text = stringResource(id = R.string.AddDream_context_label),
        icon = R.drawable.context
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lhorloge),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
                Button(
                    onClick = { showTimePicker(context) { hour, minute ->
                        onTimeChanged("$hour:$minute")
                        onContextSleepChanged(contextSleep)
                    } },
                    modifier = Modifier
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    if (time.isEmpty()) {
                        Text("Heure de coucher")
                    } else {
                        Text(time)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.thermometre),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = temperature.toFloat(),
                        onValueChange = { newTemp ->
                            onTemperatureChanged(newTemp.toInt())
                            contextSleep["temperature"] = newTemp.toInt()
                            onContextSleepChanged(contextSleep)
                        },
                        valueRange = 15f..30f,
                        steps = 15
                    )
                    Text(
                        text = "${temperature}°C",
                    )
                }
            }
        }
        Column (
            modifier = Modifier
                .weight(1f)
        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bed),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                )
                CustomDropdown(
                    options = listOf("0 Réveils", "1 Réveils", "2 Réveils", "3 Réveils", "> 3 Réveils"),
                    selectedOption = nbReveils,
                    onOptionSelected = {
                        onNbReveilsChanged(it)
                        onContextSleepChanged(contextSleep)
                                       },
                )
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Icon(
                    painter = painterResource(id = R.drawable.son),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                )
                CustomDropdown(
                    options = listOf("Calme", "Bruyant", "Normal"),
                    selectedOption = noiseLevel,
                    onOptionSelected = {
                        onNoiseLevelChanged(it)
                        onContextSleepChanged(contextSleep)
                                       },
                )
            }
        }
    }
}

@Composable
fun DreamType (
    dreamTypeChoose: String,
    onDreamTypeChanged: (String) -> Unit
) {

    TitleSection(
        text = stringResource(id = R.string.AddDream_label_typeOfDream),
        icon = R.drawable.lune
    )

    Row {
         ItemDreamType(
            icon = R.drawable.lune,
            text = "Rêve",
            dreamTypeChoose = dreamTypeChoose,
            onDreamTypeChanged = { onDreamTypeChanged("Rêve") }
        )
        ItemDreamType(
            icon = R.drawable.etoile,
            text = "Lucide",
            dreamTypeChoose = dreamTypeChoose,
            onDreamTypeChanged = { onDreamTypeChanged("Lucide") }
        )
        ItemDreamType(
            icon = R.drawable.cauchemar,
            text = "Cauchemar",
            dreamTypeChoose = dreamTypeChoose,
            onDreamTypeChanged = { onDreamTypeChanged("Cauchemar") }
        )
    }
}

@Composable
fun ItemDreamType (
    icon: Int,
    text: String,
    dreamTypeChoose: String,
    onDreamTypeChanged: () -> Unit
) {
    Button (
        onClick = {
            onDreamTypeChanged()
        },
        modifier = Modifier
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if(dreamTypeChoose == text) Color(0xFFeff2fe) else MaterialTheme.colorScheme.surface,
            contentColor = if (dreamTypeChoose == text) Color(0xFF555393) else MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Text(text)
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun OverlayAudioPlayer (
    viewModel: AudioRecorderViewModel = viewModel(
    factory = AudioRecorderViewModelFactory(LocalContext.current)
),
    onChangeShowOverlay: (Boolean) -> Unit,
    audio: MutableMap<String, Any>,
    onAudioChanged : (MutableMap<String, Any>) -> Unit,
    onPathChanged: (String) -> Unit,
    OnDurationChanged: (Int) -> Unit
    )
{

    val audioFilePath by viewModel.audioFilePath.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()

    var path by remember { mutableStateOf(audio["path"] as? String ?: "") }
    var showConfirmLeaveOverlay by remember { mutableStateOf(false) }

    if (showConfirmLeaveOverlay) {
        ConfirmDialog(
            onConfirm = {
                showConfirmLeaveOverlay = false
                viewModel.stopRecording()
                viewModel.deleteAudio()
                onChangeShowOverlay(false)
            },
            text = "Êtes-vous sûr de vouloir quitter l'enregistrement audio ?",
            title = "Supprimer l'enregistrement audio",
            onDismiss = {
                showConfirmLeaveOverlay = false
                viewModel.resumeRecording()
            }
        )
    }


    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.delete),
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    viewModel.pauseRecording()
                    showConfirmLeaveOverlay = true
                }
        )
        Text(
            text = "Enregistrement en cours : $duration s",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    viewModel.stopRecording()
                    onChangeShowOverlay(false)
                    OnDurationChanged(duration.toInt())
                    onPathChanged(audioFilePath.toString())
                    onAudioChanged(audio)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.save),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
            }

            Button(
                onClick = {
                    if (isRecording) {
                        viewModel.pauseRecording()
                    } else {
                        viewModel.resumeRecording()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = if (isRecording) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DescribeDream(
    title: String,
    content: String,
    onValueChangeTitle: (String) -> Unit = {},
    onValueChangeContent: (String) -> Unit = {},
    hasAudioPermission: MutableState<Boolean>,
    checkAudioPermission: () -> Unit = {},
    viewModel: AudioRecorderViewModel = viewModel(
        factory = AudioRecorderViewModelFactory(LocalContext.current)
    ),
    onChangeShowOverlay: (Boolean) -> Unit
    ) {

    var storage = Firebase.storage
    var storageRef = storage.reference
    var imageRef = storageRef.child(R.drawable.lune.toString())

    val audioFilePath by viewModel.audioFilePath.collectAsState()
    var isListening by remember { mutableStateOf(false) }
    val isRecording by viewModel.isRecording.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog){
        ConfirmDialog(
            onConfirm = {
                viewModel.deleteAudio()
                showConfirmDialog = false
            },
            onDismiss = {
                showConfirmDialog = false
            },
            text = "Êtes-vous sûr de vouloir supprimer l'enregistrement audio ?",
            title = "Supprimer l'enregistrement audio"
        )
    }

    DreamTextFieldCustom(
        analysisText = title,
        onTextChange = { onValueChangeTitle(it) },
        modifier = Modifier
            .fillMaxWidth(),
        placeHolder = "Titre du rêve",
        maxCharacters = 30,
        maxLine = 1,
        height = 100,
        maxHeight = 100
    )

    DreamTextFieldCustom(
        analysisText = content,
        onTextChange = { onValueChangeContent(it) },
        modifier = Modifier
            .fillMaxWidth(),
        placeHolder = "Contenu du rêve...",
        maxCharacters = 1000,
        maxLine = 5,
        height = 150,
        maxHeight = 300
    )

    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .clickable {
                    // TODO
                }
        )
        Icon(
            painter = painterResource(id = R.drawable.microphone),
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .clickable {
                    Log.d("Audio", hasAudioPermission.toString())
                    if (hasAudioPermission.value) {
                        viewModel.startRecording()
                        onChangeShowOverlay(true)
                    } else {
                        checkAudioPermission()
                    }
                }
        )
    }

    if (audioFilePath != null) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Button(
                onClick = {
                    viewModel.playAudio()
                    isListening = !isListening
                }
            ) {
                if (!viewModel.isMediaPlayerReleased()){
                    GlideImage(
                        model = R.drawable.ecouteurs,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Log.d("Audio", viewModel.isMediaPlayerReleased().toString())
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                Text("Écouter l'enregistrement")
            }
            Button(
                onClick = {
                    showConfirmDialog = !showConfirmDialog
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun Emotions (
    pickedEmotions: SnapshotStateList<String>
) {
    TitleSection(stringResource(id = R.string.AddDream_label_emotions), R.drawable.emotions)

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp,
    ) {
        ItemEmotion(
            text = "Joie",
            pickedEmotions = pickedEmotions
        )
        ItemEmotion(
            text = "Peur",
            pickedEmotions = pickedEmotions
        )
        ItemEmotion(
            text = "Confusion",
            pickedEmotions = pickedEmotions
        )
        ItemEmotion(
            text = "Paix",
            pickedEmotions = pickedEmotions
        )
        ItemEmotion(
            text = "Excitation",
            pickedEmotions = pickedEmotions
        )
        ItemEmotion(
            text = "Mélancolie",
            pickedEmotions = pickedEmotions
        )
    }
}

@Composable
fun ItemEmotion(
    text: String,
    pickedEmotions: SnapshotStateList<String>
) {
    Button(
        onClick = {
            if (pickedEmotions.contains(text)) {
                pickedEmotions.remove(text)
                Log.d("Emotions", pickedEmotions.toString())
            } else {
                pickedEmotions.add(text)
                Log.d("Emotions", pickedEmotions.toString())
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                pickedEmotions.contains(text) && text == "Peur" -> Color(0xFFfee3e1)
                pickedEmotions.contains(text) && text == "Joie" -> Color(0xFFfef9c2)
                pickedEmotions.contains(text) && text == "Confusion" -> Color(0xFFf4e8ff)
                pickedEmotions.contains(text) && text == "Paix" -> Color(0xFFdcfce7)
                pickedEmotions.contains(text) && text == "Excitation" -> Color(0xFFffeed5)
                pickedEmotions.contains(text) && text == "Mélancolie" -> Color(0xFFdceaff)
                else -> {
                    MaterialTheme.colorScheme.surface
                }
            },
            contentColor = when {
                pickedEmotions.contains(text) && text == "Peur" -> Color(0xFF8f7036)
                pickedEmotions.contains(text) && text == "Joie" -> Color(0xFF8c4f54)
                pickedEmotions.contains(text) && text == "Confusion" -> Color(0xFF5a347c)
                pickedEmotions.contains(text) && text == "Paix" -> Color(0xFF67a189)
                pickedEmotions.contains(text) && text == "Excitation" -> Color(0xFF77412c)
                pickedEmotions.contains(text) && text == "Mélancolie" -> Color(0xFF3f559e)
                else -> {
                    MaterialTheme.colorScheme.onSurface
                }
            }
        ),
    ) {
        Text(text)
    }
}

@Composable
fun Tags(
    allPersonnesTags: List<String>,
    allLieuxTags: List<String>,
    allActionsTags: List<String>,
    allSymbolesTags: List<String>,
    selectedTags: List<String>,
    onTagSelected: (String) -> Unit,
    onCustomTagAdded: (String, String) -> Unit, // Ajout d'un tag custom (nom, catégorie),
    selectedTagsColors: Map<String, Pair<Color, Color>>
) {
    var selectedOption by remember { mutableStateOf("Personnes") }
    var newTag by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {

        TitleSection(stringResource(id = R.string.AddDream_label_tags), R.drawable.tags)

        // Dropdown pour choisir la catégorie
        CustomDropdown(
            options = listOf("Personnes", "Lieux", "Actions", "Symboles"),
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it }
        )

        // Champ pour ajouter un tag personnalisé
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                placeholder = { Text("Ajouter un tag personnalisé") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (newTag.isNotEmpty()) {
                        onCustomTagAdded(newTag, selectedOption)
                        newTag = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = "Ajouter",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage des tags
        val currentTags = when (selectedOption) {
            "Personnes" -> allPersonnesTags
            "Lieux" -> allLieuxTags
            "Actions" -> allActionsTags
            "Symboles" -> allSymbolesTags
            else -> emptyList()
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            currentTags.distinct().forEach { tag ->
                val isSelected = tag in selectedTags
                val colors = selectedTagsColors[tag] ?: Pair(Color.LightGray, Color.Black)

                Button(
                    onClick = { onTagSelected(tag) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) colors.first else Color.LightGray,
                        contentColor = if (isSelected) colors.second else Color.Black
                    ),
                    modifier = Modifier.padding(6.dp)
                ) {
                    Text(tag)
                }
            }
        }
    }
}


@Composable
fun Features  (
    clarity: Int,
    onClarityChanged: (Int) -> Unit,
    emotionalImpact: Int,
    onEmotionalImpactChanged: (Int) -> Unit

    ) {
    TitleSection(stringResource(id = R.string.AddDream_characteristics_label), R.drawable.features)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Clarté du souvenir"
            )
            Slider(
                value = clarity.toFloat(),
                onValueChange = { newClarity ->
                    onClarityChanged(newClarity.toInt())
                },
                valueRange = 1f..5f,
                steps = 0
            )
        }
        Column {
            Text(
                text = "Impact émotionnel"
            )
            Slider(
                value = emotionalImpact.toFloat(),
                onValueChange = { newImpact ->
                    onEmotionalImpactChanged(newImpact.toInt())
                },
                valueRange = 1f..5f,
                steps = 0
            )
        }
    }
}


@Composable
fun Environment(
    selectedType: String,
    selectedSeason: String,
    selectedWeather: String,
    selectedColors: String,
    onTypeChanged: (String) -> Unit,
    onSeasonChanged: (String) -> Unit,
    onWeatherChanged: (String) -> Unit,
    onColorsChanged: (String) -> Unit,
    environment: MutableMap<String, Any>,
    onEnvironmentChanged: (MutableMap<String, Any>) -> Unit
) {

    TitleSection(stringResource(id = R.string.AddDream_label_environment), R.drawable.environnement)

    CustomDropdown(
        options = listOf("Intérieur", "Extérieur", "Les deux"),
        selectedOption = selectedType,
        onOptionSelected = {
            onTypeChanged(it)
            onEnvironmentChanged(environment)
        }
    )

    CustomDropdown(
        options = listOf("Hiver", "Printemps", "Été", "Automne"),
        selectedOption = selectedSeason,
        onOptionSelected = {
            onSeasonChanged(it)
            onEnvironmentChanged(environment)
        }
    )

    CustomDropdown(
        options = listOf("Pluvieux", "Ensoleillé", "Nuageux", "Neigeux"),
        selectedOption = selectedWeather,
        onOptionSelected = {
            onWeatherChanged(it)
            onEnvironmentChanged(environment)
        }
    )

    // TODO : surement changer ca par un simple textfield pour les couleurs
    CustomDropdown(
        options = listOf("Rouge", "Bleu", "Vert", "Jaune"),
        selectedOption = selectedColors,
        onOptionSelected = {
            onColorsChanged(it)
            onEnvironmentChanged(environment)
        }
    )
}

@Composable
fun AutoAnalyse (
    analysisText: String,
    onTextChange: (String) -> Unit = {}
) {
    TitleSection("Auto analyse", R.drawable.brain)

    DreamTextFieldCustom(
        analysisText = analysisText,
        onTextChange = { onTextChange(it) },
        placeHolder = "Écris ton ressenti et ton analyse ici...",
        maxCharacters = 500,
        maxLine = 5,
        height = 100,
        maxHeight = 200
    )
}

@Composable
fun DreamTextFieldCustom(
    analysisText: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeHolder: String,
    maxCharacters: Int,
    maxLine: Int,
    height: Int,
    maxHeight: Int
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = analysisText,
            onValueChange = { newText: String ->
                if (newText.length <= maxCharacters) onTextChange(newText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = height.dp, max = maxHeight.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            placeholder = { Text(placeHolder) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            maxLines = maxLine,
            singleLine = false,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { /* Fermer le clavier si nécessaire */ }
            )
        )

        Text(
            text = "${analysisText.length} / $maxCharacters",
            style = MaterialTheme.typography.bodySmall,
            color = if (analysisText.length >= maxCharacters) Color.Red else Color.Gray,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}



@Composable
fun Share  () {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(48.dp),
        onClick = { /*TODO*/ }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.share),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Text(
                text = stringResource(id = R.string.Add_dream_share_with),
                modifier = Modifier
                    .padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun TitleSection(text: String, icon: Int) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ){
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
}

val colorsBackground = listOf(
    Color(0xFFfee3e1),
    Color(0xFFfef9c2),
    Color(0xFFf4e8ff),
    Color(0xFFdcfce7),
    Color(0xFFffeed5),
    Color(0xFFdceaff)
)

val colorsContent = listOf(
    Color(0xFF8f7036),
    Color(0xFF8c4f54),
    Color(0xFF5a347c),
    Color(0xFF67a189),
    Color(0xFF77412c),
    Color(0xFF3f559e)
)
