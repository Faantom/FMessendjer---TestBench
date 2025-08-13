package com.example.fantom

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserListActivity : ComponentActivity() {
    private val database = Firebase.database
    private val usersRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserListScreen(
                usersRef = usersRef,
                onError = { message -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    usersRef: DatabaseReference,
    onError: (String) -> Unit
) {
    var users by remember { mutableStateOf(emptyList<User>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(usersRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (child in snapshot.children) {
                    child.getValue(User::class.java)?.let { userList.add(it) }
                }
                users = userList
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Ошибка загрузки данных: ${error.message}")
                isLoading = false
            }
        }
        usersRef.addValueEventListener(listener)
        onDispose { usersRef.removeEventListener(listener) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            users.isEmpty() -> Text(
                "Список пользователей пуст",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(users, key = { it.uid }) { user ->
                    // Выводим данные пользователя напрямую
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Имя: ${user.displayName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Email: ${user.email}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}