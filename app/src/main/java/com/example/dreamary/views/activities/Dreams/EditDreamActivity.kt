package com.example.dreamary.views.activities.Dreams

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.ui.theme.DreamaryTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.utils.SnackbarManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.Share
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModelFactory
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.viewmodels.dreams.DetailsDreamViewModel
import com.example.dreamary.viewmodels.dreams.DetailsDreamViewModelFactory
import com.example.dreamary.views.components.Loading
import kotlinx.coroutines.delay


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
fun EditDreamActivity (navController: NavController, viewModel: DetailsDreamViewModel = viewModel(
    factory = DetailsDreamViewModelFactory (DreamRepository(LocalContext.current), SocialRepository(LocalContext.current))
), dreamId: String
) {
    var dreamTypeChoose by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("")}
    var title by remember { mutableStateOf("")}
    val currentUser = FirebaseAuth.getInstance().currentUser
    val pickedEmotions = remember { mutableStateListOf<String>() }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showConfirmLeaveActivity by remember { mutableStateOf(false) }
    val dreamRecupered by viewModel.dream.collectAsState()
    var createdAt = Timestamp.now()

    BackHandler {
        showConfirmLeaveActivity = true
    }

    var dream by remember { mutableStateOf(Dream(
        id = dreamId,
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
        ),
        environment = mapOf(
            "dominantColors" to "",
            "season" to "",
            "type" to "",
            "weather" to ""
        ),
        metadata = mapOf(
            "createdAt" to createdAt,
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

// Liste des tags sélectionnés pour le rêve
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }

// Stocke la couleur attribuée à chaque tag sélectionné
    var selectedTagColors by remember { mutableStateOf(mutableMapOf<String, Pair<Color, Color>>()) }

    var noiseLevel by remember {
        mutableStateOf("Non renseigné")
    }
    var nbReveils by remember {
        mutableStateOf("Non renseigné")
    }
    var temperature: Long by remember {
        mutableStateOf(0)
    }
    var time by remember {
        mutableStateOf("")
    }

    var path by remember { mutableStateOf("") }
    var duration: Long by remember { mutableStateOf(0) }
    var url by remember { mutableStateOf("") }

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

        viewModel.getFriendsAndGroupForCurrentUser(currentUser?.uid ?: "")
        viewModel.getDreamById(dreamId, currentUser?.uid ?: "")
        Log.d("Dream15", dreamRecupered.toString())
        dream = dreamRecupered ?: dream
        }

        LaunchedEffect(dreamRecupered) {
            dreamRecupered?.let {
                title = it.title
                content = it.content
                dreamTypeChoose = it.dreamType
                pickedEmotions.addAll(it.emotions)
                dreamRecupered!!.tags["characters"]?.let { tagsPersonnes = it + tagsPersonnes.filter { tag -> tag !in it } }
                selectedTags = it.tags["characters"] as List<String> + it.tags["places"] as List<String> + it.tags["themes"] as List<String> + it.tags["symbols"] as List<String> + it.tags["divers"] as List<String>
                selectedTags.forEach { tag ->
                    val randomIndex = (colorsBackground.indices).random()
                    val backgroundColor = colorsBackground[randomIndex]
                    val contentColor = colorsContent[randomIndex]
                    selectedTagColors = selectedTagColors.toMutableMap().apply {
                        put(tag, Pair(backgroundColor, contentColor))
                    }
                }
                dreamRecupered!!.tags["places"]?.let { tagsLieux = it + tagsLieux.filter { tag -> tag !in it } }
                dreamRecupered!!.tags["themes"]?.let { tagsActions = it + tagsActions.filter { tag -> tag !in it } }
                dreamRecupered!!.tags["symbols"]?.let { tagsSymboles = it + tagsSymboles.filter { tag -> tag !in it } }
                noiseLevel = it.sleepContext["noiseLevel"] as String
                nbReveils = it.sleepContext["nbReveils"] as String
                temperature = it.sleepContext["temperature"] as Long
                time = it.sleepContext["time"] as String
                clarity = it.characteristics["clarity"] as Int
                emotionalImpact = it.characteristics["emotionalImpact"] as Int
                selectedType = it.environment["type"] as String
                selectedSeason = it.environment["season"] as String
                selectedWeather = it.environment["weather"] as String
                selectedColors = it.environment["dominantColors"] as String
                path = it.audio["path"] as String
                duration = it.audio["duration"] as Long
                url = it.audio["url"] as String
                analysisText = it.analysis
                createdAt = it.createdAt
        }

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

    val savingInProgress = remember { mutableStateOf(false) }

    var listPeopleShareWith: Share by remember { mutableStateOf(Share(listOf<User>(), listOf<Group>())) }
    val listFriendsAndGroup: Share by viewModel.friendsAndGroup.collectAsState()

    DreamaryTheme {
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    if (!savingInProgress.value) {
                        TopbarUpdateActivity(navController, viewModel, coroutineScope, dream, savingInProgress, dreamTypeChoose)
                    }
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
                            onTemperatureChanged = { temperature = it.toLong() },
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
                            showOverlay,
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
                                    Log.d("SelectedTags", "Removing $tag")
                                    // Si le tag est déjà sélectionné, on le retire et on supprime sa couleur associée
                                    selectedTags = selectedTags - tag
                                    selectedTagColors = selectedTagColors.toMutableMap().apply { remove(tag) }
                                } else {
                                    Log.d("SelectedTags", "Adding $tag")
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
                        Share(
                            listFriendsAndGroup = listFriendsAndGroup,
                            onChanges = { listPeopleShareWith = it },
                            onClear = { listPeopleShareWith = Share(listOf(), listOf()) },
                            listPeopleShareWith = listPeopleShareWith
                        )
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
                            onDurationChanged = { duration = it.toLong() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopbarUpdateActivity (navController: NavController, viewModel: DetailsDreamViewModel, coroutineScope: CoroutineScope, dream: Dream, savingInProgress: MutableState<Boolean>, dreamTypeChoose: String) {
    var showConfirmLeave by remember { mutableStateOf(false) }
    dream.lucid = dreamTypeChoose == "Lucide"

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            onClick = {
                Log.d("test1232", dream.toString())
                savingInProgress.value = true
                viewModel.updateDream(
                    navController = navController,
                    dream = dream,
                    coroutineScope = coroutineScope,
                    onSaved = {
                        savingInProgress.value = false
                        CoroutineScope(coroutineScope.coroutineContext).launch {
                            SnackbarManager.showMessage("Rêve modifié avec succès", R.drawable.success)
                            delay(2000)
                            navController.navigate(NavRoutes.DreamDetail.createRoute(dream.id)){
                                popUpTo(NavRoutes.EditDream.route) {
                                    inclusive = true
                                }
                                popUpTo(NavRoutes.DreamDetail.route) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onFailure = {
                        savingInProgress.value = false
                    }
                )
            }
        ) {
            Text(
                text = "Modifier",
            )
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
            text = "Êtes-vous sûr de vouloir annuler les modifications ?",
            title = "Quitter sans enregistrer"
        )
    }
}