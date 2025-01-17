package com.example.dreamary.views.activities.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dreamary.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dreamary.models.repositories.AuthRepository
import com.example.dreamary.models.repositories.AuthResponse
import com.example.dreamary.viewmodels.auth.LoginViewModel
import com.example.dreamary.viewmodels.auth.LoginViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Preview(showBackground = true)
@Composable
private fun PreviewLoginActivity() {
    val previewNavController = rememberNavController()
    LoginActivity(navController = previewNavController)
}

@Composable
fun LoginActivity(navController: NavController,  viewModel: LoginViewModel = viewModel(
    factory = LoginViewModelFactory(AuthRepository(LocalContext.current))
)) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(painterResource(id = R.drawable.background), contentScale = ContentScale.Crop)
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
                text = stringResource(id = R.string.app_name),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = stringResource(id = R.string.slogan),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp, top = 2.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "Email",
                        color = Color.White
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
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
                        color = Color.White
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp, top = 5.dp)
            )

            Button(
                onClick = {
                    viewModel.createAccountWithEmail(email, password)
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
                Text(stringResource(id = R.string.btn_connect_login))
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
                    viewModel.signInWithGoogle(navController)
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
                    .padding(top = 16.dp),
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

            Text(
                text = stringResource(id = R.string.Login_forgot_password),
                color = Color(Color(0xFF6200EE).toArgb()),
                modifier = Modifier
                    .padding(top = 16.dp)
            )

            Row {
                Text(
                    text = stringResource(id = R.string.Login_no_account),
                    modifier = Modifier.padding(top = 16.dp),
                    Color(Color(0xFFFFFFFF).toArgb())
                )
                Text(
                    text = stringResource(id = R.string.Login_register),
                    modifier = Modifier.padding(top = 16.dp, start = 4.dp),
                    color = Color(Color(0xFF6200EE).toArgb())
                )
            }
        }
    }
}