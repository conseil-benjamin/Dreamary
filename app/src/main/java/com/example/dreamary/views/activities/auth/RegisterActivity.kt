package com.example.dreamary.views.activities.auth

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dreamary.R
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.AuthResponse
import com.example.dreamary.models.routes.NavRoutes
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.viewmodels.auth.RegisterViewModel
import com.example.dreamary.viewmodels.auth.RegisterViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun PreviewRegisterActivity() {
    val previewNavController = rememberNavController()
    RegisterActivity(navController = previewNavController)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterActivity(navController: NavController, viewModel: RegisterViewModel = viewModel(
    factory = RegisterViewModelFactory (AuthRepository(LocalContext.current))
)) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRulesAccepted by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    // Ecoute des messages du SnackbarManager
    LaunchedEffect(Unit) { // unit veut dire que l'effet sera lancé une seule fois
        SnackbarManager.snackbarMessages.collect { snackbarMessage ->
            snackbarHostState.showSnackbar(
                message = snackbarMessage.message,
                actionLabel = snackbarMessage.actionLabel
            )
        }
    }

    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.background),
                    contentScale = ContentScale.Crop
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.play_store_512),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = stringResource(id = R.string.Register_text_join_app),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = stringResource(id = R.string.Register_slogan),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp, top = 2.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.Register_input_name),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = "Email",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.login_field_password),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if(isPasswordVisible) {
                            // icone pour rendre le mot de passe invisible
                            Icon(
                                painter = painterResource(id = R.drawable.hide_password), contentDescription = "see password",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                    isPasswordVisible = !isPasswordVisible
                                }
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.see_password), contentDescription = "see password",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                    isPasswordVisible = !isPasswordVisible
                                }
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.Register_input_confirm_password),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.Register_password_strenght_informations),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRulesAccepted,
                        onCheckedChange = { isChecked -> isRulesAccepted = isChecked },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.Register_accept_rules),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = {
                        viewModel.createAccountWithEmail(
                            context,
                            email,
                            password,
                            confirmPassword,
                            navController,
                            isRulesAccepted
                        )
                            .onEach { response: Any ->
                                if (response is AuthResponse.Success) {
                                    println("Success")
                                } else {
                                    println("Error")
                                    Log.i("logGoogle", "error")
                                }
                            }
                            .launchIn(coroutineScope)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        contentColor = Color(0xFFFFFFFF),
                    ),
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp, top = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.Register_create_account))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.Gray)
                    )
                    Text(
                        text = stringResource(id = R.string.Login_ou),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color.Gray)
                    )
                }
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        contentColor = Color(0xFFFFFFFF),
                    ),
                    onClick = {
                        viewModel.signUpWithGoogle(navController)
                            .onEach { response ->
                                if (response is AuthResponse.Success) {
                                    println("Success")
                                } else {
                                    println("Error")
                                    Log.i("logGoogle", "error")
                                }
                            }
                            .launchIn(coroutineScope)
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(stringResource(id = R.string.Login_btn_connexion_google))
                }

                Row {
                    Text(
                        text = stringResource(id = R.string.Register_text_already_account),
                        modifier = Modifier.padding(top = 16.dp),
                        Color(Color(0xFFFFFFFF).toArgb())
                    )
                    Text(
                        text = stringResource(id = R.string.Register_text_redirection_login),
                        modifier = Modifier
                            .padding(top = 16.dp, start = 4.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate(NavRoutes.Login.route)
                                }
                            ),
                        color = Color(Color(0xFF6200EE).toArgb())
                    )
                }
            }
        }
    }
}