package com.example.fantom

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class UserDataActivity : ComponentActivity() {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRepository = UserRepository(this)

        setContent {
            MaterialTheme {
                UserDataScreen(userRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDataScreen(userRepository: UserRepository) {
    val context = LocalContext.current
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val uid = firebaseUser?.uid ?: ""

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (displayName.isBlank() || email.isBlank()) {
                    Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (uid.isBlank()) {
                    Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val user = User(uid = uid, displayName = displayName.trim(), email = email.trim())
                userRepository.saveUser(
                    user,
                    onSuccess = {
                        Toast.makeText(context, "Пользователь сохранён", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (uid.isBlank()) {
                    Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                userRepository.loadUser(uid, object : UserRepository.UserCallback {
                    override fun onUserLoaded(user: User?) {
                        if (user != null) {
                            displayName = user.displayName
                            email = user.email
                            Toast.makeText(context, "Пользователь загружен", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(e: Exception) {
                        Toast.makeText(context, "Ошибка загрузки: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить")
        }
    }
}