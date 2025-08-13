package com.example.fantom

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                UserProfileScreen(
                    onNextClicked = {
                        startActivity(Intent(this, ChatsActivity::class.java))
                        finish()
                    },
                    onSaveName = { name ->
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Имя успешно сохранено!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, ChatsActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Ошибка при сохранении имени.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Пользователь не авторизован.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserProfileScreen(onNextClicked: () -> Unit, onSaveName: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val isNameValid = name.trim().isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.back_main_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Введите ваше имя", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onSaveName(name) },
                enabled = isNameValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Далее")
            }
        }
    }
}