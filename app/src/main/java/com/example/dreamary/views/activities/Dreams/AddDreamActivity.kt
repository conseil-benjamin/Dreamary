package com.example.dreamary.views.activities.Dreams

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.example.dreamary.R
import com.example.dreamary.ui.theme.DreamaryTheme

@Preview(showBackground = true)
@Composable
fun AddDreamActivityPreview() {
    AddDreamActivity(navController = NavController(LocalContext.current))
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddDreamActivity (navController: NavController) {
    Text("AddDreamActivity")

    DreamaryTheme {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                Topbar(navController)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            ) {
                ContextSleep()
                DreamType()
                DescribeDream()
                Emotions()
                Tags()
                Features()
                Share()
            }
        }
    }
}

@Composable
fun Topbar (navController: NavController) {
    Row (
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            "Annuler",
            modifier = androidx.compose.ui.Modifier
                .weight(1f)
                .clickable { navController.popBackStack() }
        )
        Text(
            "Brouillon",
            modifier = androidx.compose.ui.Modifier.weight(2f)
        )
        Button(
            onClick = { /*TODO*/ }
        ) {
            Text("Enregistrer")
        }
    }

}

@Composable
fun ContextSleep () {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(16.dp)
    ) {
        Column (
            modifier = Modifier
                .weight(1f)
        ) {
            Text("ContextSleep")
        }

        Column (
            modifier = Modifier
                .weight(1f)
        ) {
            Text("ContextSleep")
        }
    }

}

@Composable
fun DreamType () {
    Text("DreamType")
}

@Composable
fun DescribeDream () {
    Text("DescribeDream")
    var storage = Firebase.storage
    var storageRef = storage.reference
    var imageRef = storageRef.child(R.drawable.lune.toString())

    //utiliser l'uri de l'image pour la mettre dans le storage

    Button(
        onClick = {
           // imageRef.putFile(R.drawable.lune)
        }
    ) {
        Text("Ajouter une image")
    }
}

@Composable
fun Emotions () {
    Text("Emotions")
}

@Composable
fun Tags () {
    Text("Tags")
}

@Composable
fun Features  () {
    Text("Features")
}

@Composable
fun Share  () {
    Text("Share")
}