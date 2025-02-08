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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.zIndex
import coil.request.Tags
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModel
import com.example.dreamary.viewmodels.audio.AudioRecorderViewModelFactory
import com.example.dreamary.views.components.CustomDropdown
import java.util.Calendar

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
private fun ConfirmDeleteAudio(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer l'enregistrement audio") },
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
    val dreamTypeChoose = remember { mutableStateOf("Rêve") }
    var content by remember { mutableStateOf("")}
    var title by remember { mutableStateOf("")}
    var date by remember { mutableStateOf("")}
    val currentUser = FirebaseAuth.getInstance().currentUser
    val pickedEmotions = remember { mutableStateListOf("") }
    val pickedTags = remember { mutableStateListOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    var dream = Dream(
        title = title,
        content = content,
        dreamType = dreamTypeChoose.value,
        isLucid = dreamTypeChoose.value == "Lucide",
        isShared = false,
        analysis = "",
        emotions = pickedEmotions,
        userId = currentUser?.uid ?: "",
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
            "position" to "",
            "temperature" to 0,
            "time" to "",
            "quality" to 0,
            "duration" to 0
        ),
        social = mapOf(
            "likes" to 0,
            "comments" to 0
        ),
        tags = mapOf(
            "symbols" to listOf<String>(),
            "themes" to listOf<String>(),
            "characters" to listOf<String>(),
            "places" to listOf<String>()
        ),
    )

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

    // Mettre à jour dream.tags à chaque fois que les tags changent
    LaunchedEffect(tagsCharacters, tagsPlaces, tagsThemes, tagsSymbols) {
        dream = dream.copy(
            tags = mapOf(
                "characters" to tagsCharacters,
                "places" to tagsPlaces,
                "themes" to tagsThemes,
                "symbols" to tagsSymbols
            )
        )
    }

    var selectedType by remember {
        mutableStateOf("")
    }
    var selectedSeason by remember {
        mutableStateOf("")
    }
    var selectedWeather by remember {
        mutableStateOf("")
    }
    var selectedColors by remember {
        mutableStateOf("")
    }

    LaunchedEffect(selectedType, selectedSeason, selectedWeather, selectedColors) {
        Log.d("Environment", "Type: $selectedType, Season: $selectedSeason, Weather: $selectedWeather, Colors: $selectedColors")
        dream = dream.copy(
            environment = mapOf(
                "dominantColors" to selectedColors,
                "season" to selectedSeason,
                "type" to selectedType,
                "weather" to selectedWeather
            )
        )
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

    val context = LocalContext.current
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

    DreamaryTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    Topbar(navController, viewModel, coroutineScope, dream)
                }
            ) { paddingValues ->
                LazyColumn(
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        ContextSleep(
                            contextSleep = dream.sleepContext as MutableMap<String, Any>,
                            onContextSleepChanged = { dream.sleepContext = it }
                        )
                    }

                    item {
                        DreamType(
                            dreamTypeChoose = dreamTypeChoose
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
                            tagsCharacters = tagsCharacters,
                            tagsPlaces = tagsPlaces,
                            tagsThemes = tagsThemes,
                            tagsSymbols = tagsSymbols,
                            onTagsCharactersChanged = { tagsCharacters = it },
                            onTagsPlacesChanged = { tagsPlaces = it },
                            onTagsThemesChanged = { tagsThemes = it },
                            onTagsSymbolsChanged = { tagsSymbols = it },
                            tags = dream.tags as MutableMap<String, Any>,
                            onTagsChanged = { dream.tags = it }
                        )
                    }

                    item {
                        Features()
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
                        Share()
                    }
                }
            }
            if (showOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        .zIndex(10f)
                        .clickable { showOverlay = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            onAudioChanged = { dream.audio = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Topbar (navController: NavController, viewModel: AddDreamViewModel, coroutineScope: CoroutineScope, dream: Dream) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            "Annuler",
            modifier = Modifier
                .weight(1f)
                .clickable { navController.popBackStack() }
        )
        Button(
            onClick = {
                Log.d("test1232", dream.toString())
                viewModel.addDream(
                    navController = navController,
                    dream = dream,
                    coroutineScope = coroutineScope
                )
            }
        ) {
            Text("Enregistrer")
        }
    }
}

@Composable
fun ItemsleepInfo(
    icon: Int,
    modifier: Modifier,
    data: Array<String>
) {
    Row (
        modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
        DropdownMenu(
            modifier = Modifier
                .weight(1f),
            expanded = false,
            onDismissRequest = {false}
        ) {
            data.forEach {
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /*TODO*/ }
                )
            }
        }
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
    contextSleep: MutableMap<String, Any>,
    onContextSleepChanged: (MutableMap<String, Any>) -> Unit
) {
    var noiseLevel by remember { mutableStateOf(contextSleep["noiseLevel"] as? String ?: "Calme") }
    var position by remember { mutableStateOf(contextSleep["position"] as? String ?: "Sur le dos") }
    var temperature by remember { mutableStateOf(contextSleep["temperature"] as? String ?: "") }

    val context = LocalContext.current
    var time by remember { mutableStateOf(contextSleep["time"] as? String ?: "") }

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
                        contextSleep["time"] = "$hour:$minute"
                        onContextSleepChanged(contextSleep)
                        time = "$hour:$minute"
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
                TextField(
                    value = temperature,
                    onValueChange = {
                        temperature = it
                        onContextSleepChanged(contextSleep)
                        contextSleep["temperature"] = it
                                    },
                    label = { Text("Température") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                )
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
                    selectedOption = position,
                    onOptionSelected = {
                        position = it
                        onContextSleepChanged(contextSleep)
                        contextSleep["position"] = it
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
                        noiseLevel = it
                        onContextSleepChanged(contextSleep)
                        contextSleep["noiseLevel"] = it },
                )
            }
        }
    }
}

@Composable
fun DreamType (
    dreamTypeChoose: MutableState<String>
) {
    Text(text = stringResource(id = R.string.Add_Dream_Screen_Type_Of_Dream))
    Row {
         ItemDreamType(
            icon = R.drawable.lune,
            text = "Rêve",
             dreamTypeChoose = dreamTypeChoose
        )
        ItemDreamType(
            icon = R.drawable.etoile,
            text = "Lucide",
            dreamTypeChoose = dreamTypeChoose
        )
        ItemDreamType(
            icon = R.drawable.cauchemar,
            text = "Cauchemar",
            dreamTypeChoose = dreamTypeChoose
        )
    }
}

@Composable
fun ItemDreamType (
    icon: Int,
    text: String,
    dreamTypeChoose: MutableState<String>
) {
    Button (
        onClick = {
            dreamTypeChoose.value = text
        },
        modifier = Modifier
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if(dreamTypeChoose.value == text) Color(0xFFeff2fe) else MaterialTheme.colorScheme.surface,
            contentColor = if (dreamTypeChoose.value == text) Color(0xFF555393) else MaterialTheme.colorScheme.onSurface,
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
    onAudioChanged : (MutableMap<String, Any>) -> Unit
    )
{

    val audioFilePath by viewModel.audioFilePath.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()

    var path by remember { mutableStateOf(audio["path"] as? String ?: "") }
    var showConfirmLeaveOverlay by remember { mutableStateOf(false) }

    if (showConfirmLeaveOverlay) {
        ConfirmDeleteAudio(
            onConfirm = {
                showConfirmLeaveOverlay = false
                viewModel.stopRecording()
                viewModel.deleteAudio()
                onChangeShowOverlay(false)
            },
            text = "Êtes-vous sûr de vouloir quitter l'enregistrement audio ?",
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
                    Log.d("Audio1223", audioFilePath.toString())
                    viewModel.stopRecording()
                    onChangeShowOverlay(false)
                    onAudioChanged(audio)
                    audio["path"] = audioFilePath.toString()
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
        ConfirmDeleteAudio(
            onConfirm = {
                viewModel.deleteAudio()
                showConfirmDialog = false
            },
            onDismiss = {
                showConfirmDialog = false
            },
            text = "Êtes-vous sûr de vouloir supprimer l'enregistrement audio ?"
        )
    }

    TextField(
        value = title,
        onValueChange = { onValueChangeTitle(it) },
        //label = { Text("Titre du rêve") },
        placeholder = { Text("Titre du rêve") },
        modifier = Modifier.fillMaxWidth()
    )

    TextField(
        value = content,
        onValueChange = { onValueChangeContent(it) },
        placeholder = { Text("Contenu du rêve") },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        trailingIcon = {
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
        }
    )

    // TODO uniquement garder le bouton supprimer l'audio sur l'UI principale
    // TODO : et donc dans l'overlay afficher les boutons pour faire pause, play, stop, supprimer

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
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = R.drawable.emotions),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
        Text(
            text = "Emotions ressenties",
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
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
fun Tags (
    tagsCharacters: List<String>,
    tagsPlaces: List<String>,
    tagsThemes: List<String>,
    tagsSymbols: List<String>,
    onTagsCharactersChanged: (List<String>) -> Unit,
    onTagsPlacesChanged: (List<String>) -> Unit,
    onTagsThemesChanged: (List<String>) -> Unit,
    onTagsSymbolsChanged: (List<String>) -> Unit,
    tags: MutableMap<String, Any>,
    onTagsChanged: (MutableMap<String, Any>) -> Unit,
) {
    var selectedOption by remember { mutableStateOf("Personnes") }
    var tag by remember { mutableStateOf("") }

    var tagsPersonnes = mutableMapOf<String, List<String>>(
        "tags" to listOf("Famille", "Amis", "Inconnus", "Célébrités", "Amie", "Enfant", "Conjoint", "Collègue", "Voisin", "Inconnu")
    )
    var tagsLieux = mutableMapOf<String, List<String>>(
        "tags" to listOf("Maison", "Travail", "École", "Nature", "Ville", "Campagne", "Mer", "Montagne", "Forêt", "Rue", "Chambre", "Cuisine", "Salle de bain", "Salon", "Jardin", "Parc", "Plage", "Montagne", "Forêt", "Rue", "Chambre", "Cuisine", "Salle de bain", "Salon", "Jardin", "Parc", "Plage")
    )
    var tagsActions = mutableMapOf<String, List<String>>(
        "tags" to listOf("Courir", "Marcher", "Voler", "Nager", "Manger", "Boire", "Dormir", "Parler", "Écouter", "Regarder", "Lire", "Écrire", "Travailler", "Étudier", "Jouer", "Danser", "Chanter", "Rire", "Pleurer", "Aider", "Sauver", "Tuer", "Blesser", "Aimer", "Détester", "Crier", "Prier", "Méditer", "Rêver", "Voyager", "Conduire", "Voyager", "Conduire")
    )
    var tagsSymboles = mutableMapOf<String, List<String>>(
        "tags" to listOf("Lumière", "Obscurité", "Feu", "Eau", "Terre", "Air", "Ciel", "Enfer", "Paradis", "Mort", "Vie", "Naissance", "Famille", "Amour", "Haine", "Paix", "Guerre", "Richesse", "Pauvreté", "Santé", "Maladie", "Bonheur", "Tristesse", "Joie", "Peur", "Colère", "Amour", "Haine", "Paix", "Guerre", "Richesse", "Pauvreté", "Santé", "Maladie", "Bonheur", "Tristesse", "Joie", "Peur", "Colère")
    )

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.tags),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Text(
                text = "Tags",
                modifier = Modifier
                    .padding(start = 16.dp)
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row (
                modifier = Modifier
                    .weight(3f)
            ) {
                CustomDropdown(
                    options = listOf("Personnes", "Lieux", "Actions", "Symboles", "Divers"),
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption = it },
                )
            }
            Row (
                modifier = Modifier
                    .weight(6f)
            ) {
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    placeholder = { Text("Ajouter un tag personnalisé") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.plus),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        }
        Column {
             when (selectedOption) {
                "Personnes" -> {
                    FlowRow (
                        modifier = Modifier
                            .fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                    ){
                        tagsPersonnes["tags"]?.forEach {
                            val number = generateRandomInt(0..5)
                            Button(
                                onClick = {
                                    val newTagsList = if (tagsCharacters.contains(it)) {
                                        tagsCharacters - it
                                    } else {
                                        tagsCharacters + it
                                    }
                                    onTagsCharactersChanged(newTagsList)
                                    tags["characters"] = newTagsList
                                    onTagsChanged(tags)
                                    Log.d("Tags", tags.toString())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tagsCharacters.contains(it)) colorsBackground[number] else MaterialTheme.colorScheme.surface,
                                    contentColor = if (tagsCharacters.contains(it)) colorsContent[number] else MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                            {
                                Text(it)
                            }
                        }
                }
                }
                "Lieux" -> {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                    ) {
                        tagsLieux["tags"]?.forEach {
                            val number = generateRandomInt(0..5)
                            Button(
                                onClick = {
                                    val newTagsList = if (tagsPlaces.contains(it)) {
                                        tagsPlaces - it
                                    } else {
                                        tagsPlaces + it
                                    }
                                    onTagsPlacesChanged(newTagsList)
                                    tags["places"] = newTagsList
                                    onTagsChanged(tags)
                                    Log.d("Tags", tags.toString())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tagsPlaces.contains(it)) colorsBackground[number] else MaterialTheme.colorScheme.surface,
                                    contentColor = if (tagsPlaces.contains(it)) colorsContent[number] else MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(it)
                            }
                        }
                    }
                }
                "Actions" -> {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                    ) {
                        tagsActions["tags"]?.forEach {
                            val number = generateRandomInt(0..5)
                            Button(
                                onClick = {
                                    val newTagsList = if (tagsThemes.contains(it)) {
                                        tagsThemes - it
                                    } else {
                                        tagsThemes + it
                                    }
                                    onTagsThemesChanged(newTagsList)
                                    tags["themes"] = newTagsList
                                    onTagsChanged(tags)
                                    Log.d("Tags", tags.toString())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tagsThemes.contains(it)) colorsBackground[number] else MaterialTheme.colorScheme.surface,
                                    contentColor = if (tagsThemes.contains(it)) colorsContent[number] else MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(it)
                            }
                        }
                    }
                }
                "Symboles" -> {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                    ) {
                        tagsSymboles["tags"]?.forEach {
                            val number = generateRandomInt(0..5)
                            Button(
                                onClick = {
                                    val newTagsList = if (tagsSymbols.contains(it)) {
                                        tagsSymbols - it
                                    } else {
                                        tagsSymbols + it
                                    }
                                    onTagsSymbolsChanged(newTagsList)
                                    tags["symbols"] = newTagsList
                                    onTagsChanged(tags)
                                    Log.d("Tags", tags.toString())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tagsSymbols.contains(it)) colorsBackground[number] else MaterialTheme.colorScheme.surface,
                                    contentColor = if (tagsSymbols.contains(it)) colorsContent[number] else MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Features  () {
    Text("Caractéristiques")

    Row {
        Text(
            text = "Clarté du souvenir"
        )
    }
}


// TODO : environnement dans dream n'est pas mis à jour correctement
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

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ){
        Icon(
            painter = painterResource(id = R.drawable.environnement),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
        Text(
            text = "Environnement",
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
    CustomDropdown(
        options = listOf("Intérieur", "Extérieur", "Les deux"),
        selectedOption = selectedType,
        onOptionSelected = { onTypeChanged(it) }
    )

    CustomDropdown(
        options = listOf("Hiver", "Printemps", "Été", "Automne"),
        selectedOption = selectedSeason,
        onOptionSelected = { onSeasonChanged(it) }
    )

    CustomDropdown(
        options = listOf("Pluvieux", "Ensoleillé", "Nuageux", "Neigeux"),
        selectedOption = selectedWeather,
        onOptionSelected = { onWeatherChanged(it) }
    )

    // TODO : surement changer ca par un simple textfield pour les couleurs
    CustomDropdown(
        options = listOf("Rouge", "Bleu", "Vert", "Jaune"),
        selectedOption = selectedColors,
        onOptionSelected = { onColorsChanged(it)
            Log.d("Colors", environment.toString())
        }
    )
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

fun generateRandomInt(range: IntRange): Int {
    return range.random()
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
