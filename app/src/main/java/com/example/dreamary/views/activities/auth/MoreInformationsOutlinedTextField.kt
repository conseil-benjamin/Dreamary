package com.example.dreamary.views.activities.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreInformationsOutlinedTextField(
    analysisText: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
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
            ),
            label = {
                Text(
                    text = label,
                    color = Color.Gray,
                )
            },
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
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