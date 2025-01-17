package com.example.dreamary.views.activities.auth

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dreamary.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation


@Preview(showBackground = true)
@Composable
private fun PreviewLoginActivity() {
    LoginActivity()
}

@Composable
fun LoginActivity() {
    var password by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}

    Column(
        modifier = Modifier.fillMaxSize(),
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
            )
        Text(text = stringResource(id = R.string.slogan), modifier = Modifier.padding(bottom = 16.dp, top = 2.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = {
                Text(text = "Email")
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
                Text(text = stringResource(id = R.string.login_field_password))
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
            onClick = { /*TODO*/ },
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
            onClick = { /*TODO*/ },
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


        Row () {
            Text(
                text = stringResource(id = R.string.Login_no_account),
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.Login_register),
                modifier = Modifier.padding(top = 16.dp, start = 4.dp),
                color = Color(Color(0xFF6200EE).toArgb())
            )
        }
    }
}