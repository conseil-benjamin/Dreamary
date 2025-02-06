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
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.zIndex
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
fun AddDreamActivity (navController: NavController, viewModel: AddDreamViewModel = viewModel(
    factory = AddDreamViewModelFactory (DreamRepository(LocalContext.current))
)) {
    val dreamTypeChoose = remember { mutableStateOf("Rêve") }
    var content by remember { mutableStateOf("")}
    var title by remember { mutableStateOf("")}
    var date by remember { mutableStateOf("")}
    val currentUser = FirebaseAuth.getInstance().currentUser
    val pickedEmotions = remember { mutableStateListOf("") }
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
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            },
            onDismiss = {
                showPermissionDialog = false
            },
            text = "Cette application a besoin de la permission d'accès au microphone et de stockage pour enregistrer l'audio."
        )
    }

    // Vérification initiale de la permission
    fun checkAudioPermission(): () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
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
                        Tags()
                    }

                    item {
                        Features()
                    }
                    item {
                        Environment(
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
                            .clickable {}
                    ) {
                        OverlayAudioPlayer(
                            viewModel = viewModel(
                                factory = AudioRecorderViewModelFactory(LocalContext.current)
                            ),
                            onChangeShowOverlay = { showOverlay = it },
                            showOverlay = showOverlay,
                            dreamAudio = dream.audio
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
            .height(76.dp)
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            "Annuler",
            modifier = Modifier
                .weight(1f)
                .clickable { navController.popBackStack() }
        )
        Text(
            "Brouillon",
            modifier = Modifier.weight(2f)
        )
        Button(
            onClick = {
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
                    options = listOf("Sur le dos", "Sur le ventre", "Sur le côté"),
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
    showOverlay : Boolean,
    onChangeShowOverlay: (Boolean) -> Unit,
    dreamAudio: Map<String, Any>,
    )
{

    val audioFilePath by viewModel.audioFilePath.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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
                    (dreamAudio as MutableMap<String, Any>)["path"] = audioFilePath.toString()
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
    hasAudioPermission: MutableState<Boolean> = mutableStateOf(false),
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

    TextField(
        value = title,
        onValueChange = { onValueChangeTitle(it) },
        //label = { Text("Titre du rêve") },
        placeholder = { Text("Titre du rêve") },
        modifier = Modifier.fillMaxWidth()
    )

    // TODO : Afficher l'audio enregistrée pour montrer que c'est bien enregistré
    // TODO : possibilité de supprimer l'audio enregistrée et relancer l'enregistrement
    // TODO : cela supprimerait l'ancien et enregistrera un nouveau
    // TODO : possibilité également d'écouter l'audio directement depuis la page d'ajout de rêves

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
                            onChangeShowOverlay(true)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Button(
                onClick = {
                    viewModel.deleteAudio()
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
            Text("Ecouter l'enregistrement")
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun Emotions (
    pickedEmotions: SnapshotStateList<String>
) {
    Text("Emotions ressenties")
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
fun Tags () {
    Text("Tags")
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

@Composable
fun Environment(
    environment: MutableMap<String, Any>,
    onEnvironmentChanged: (MutableMap<String, Any>) -> Unit
) {
    var selectedType by remember { mutableStateOf(environment["type"] as? String ?: "Intérieur") }
    var selectedSeason by remember { mutableStateOf(environment["season"] as? String ?: "Hiver") }
    var selectedWeather by remember { mutableStateOf(environment["weather"] as? String ?: "Pluvieux") }
    var selectedColors by remember { mutableStateOf(environment["dominantColors"] as? String ?: "Rouge") }

    Log.i("environment", environment.toString())

    Text("Environnement")
    CustomDropdown(
        options = listOf("Intérieur", "Extérieur", "Les deux"),
        selectedOption = selectedType,
        onOptionSelected = {
            selectedType = it
            onEnvironmentChanged(environment)
            environment["type"] = it
        },
    )

    CustomDropdown(
        options = listOf("Hiver", "Printemps", "Été", "Automne"),
        selectedOption = selectedSeason,
        onOptionSelected = {
            selectedSeason = it
            onEnvironmentChanged(environment)
            environment["season"] = it
        },
    )

    CustomDropdown(
        options = listOf("Pluvieux", "Ensoleillé", "Nuageux", "Neigeux"),
        selectedOption = selectedWeather,
        onOptionSelected = {
            selectedWeather = it
            onEnvironmentChanged(environment)
            environment["weather"] = it
        },
    )

    // TODO : surement changer ca par un simple textfield pour les couleurs
    CustomDropdown(
        options = listOf("Rouge", "Bleu", "Vert", "Jaune"),
        selectedOption = selectedColors,
        onOptionSelected = {
            selectedColors = it
            onEnvironmentChanged(environment)
            environment["dominantColors"] = it
        },
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
