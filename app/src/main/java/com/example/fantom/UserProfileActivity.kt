package com.example.fantom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.example.fantom.ui.theme.FantomTheme

class UserProfileActivity : ComponentActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private var originalName by mutableStateOf("")
    private var originalStatus by mutableStateOf("")
    private var originalAvatarUrl by mutableStateOf("")
    private var isSaving by mutableStateOf(false)
    private var googleDisplayName by mutableStateOf("")
    private var googlePhotoUrl by mutableStateOf("")

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Разрешение на доступ к галерее отклонено", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRepository = UserRepository(this)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserData()

        setContent {
            FantomTheme {
                UserProfileScreen(
                    onBackClicked = { onBackPressed() },
                    onSaveClicked = { newName, newStatus -> saveUserData(newName, newStatus) },
                    onAvatarClicked = { checkAndRequestPermissions() },
                    selectedImageUri = selectedImageUri,
                    originalAvatarUrl = originalAvatarUrl,
                    originalName = originalName,
                    originalStatus = originalStatus,
                    isSaving = isSaving,
                    googleDisplayName = googleDisplayName,
                    googlePhotoUrl = googlePhotoUrl,
                    onResetAvatar = { selectedImageUri = null } // Call to reset the new avatar selection
                )
            }
        }
    }

    private fun loadUserData() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            originalName = firebaseUser.displayName ?: ""
            originalAvatarUrl = firebaseUser.photoUrl?.toString() ?: ""
            googleDisplayName = firebaseUser.displayName ?: ""
            googlePhotoUrl = firebaseUser.photoUrl?.toString() ?: ""

            userRepository.loadUser(firebaseUser.uid, object : UserRepository.UserCallback {
                override fun onUserLoaded(user: User?) {
                    if (user != null) {
                        originalStatus = user.status
                    } else {
                        originalStatus = ""
                    }
                }
                override fun onError(e: Exception) {
                    Toast.makeText(this@UserProfileActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveUserData(newName: String, newStatus: String) {
        val user = auth.currentUser
        if (user != null) {
            isSaving = true
            if (selectedImageUri != null) {
                // Upload only if a new image was selected
                uploadImageAndSaveData(user, newName, newStatus)
            } else {
                // Otherwise, use the existing avatar URL (original or Google's)
                updateUserProfile(user, newName, newStatus, user.photoUrl?.toString() ?: "")
            }
        }
    }

    private fun uploadImageAndSaveData(user: com.google.firebase.auth.FirebaseUser, newName: String, newStatus: String) {
        val storageRef = storage.reference.child("avatars/${user.uid}")
        selectedImageUri?.let { uri ->
            // Using a stream to be more robust with content URIs
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    storageRef.putStream(inputStream)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                val avatarUrl = downloadUri.toString()
                                updateUserProfile(user, newName, newStatus, avatarUrl)
                            }
                        }
                        .addOnFailureListener {
                            isSaving = false
                            Toast.makeText(this, "Ошибка загрузки аватара: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    isSaving = false
                    Toast.makeText(this, "Ошибка: не удалось получить файл из галереи.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isSaving = false
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserProfile(firebaseUser: com.google.firebase.auth.FirebaseUser, newName: String, newStatus: String, newAvatarUrl: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .setPhotoUri(Uri.parse(newAvatarUrl))
            .build()
        firebaseUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveDataToFirestore(firebaseUser, newName, newStatus, newAvatarUrl)
                } else {
                    isSaving = false
                    Toast.makeText(this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveDataToFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser, name: String?, status: String, avatarUrl: String) {
        val updatedUser = User(
            uid = firebaseUser.uid,
            displayName = name ?: "",
            email = firebaseUser.email ?: "",
            avatarUrl = avatarUrl,
            status = status
        )
        userRepository.saveUser(updatedUser,
            onSuccess = {
                Toast.makeText(this, "Профиль успешно обновлен", Toast.LENGTH_SHORT).show()
                isSaving = false
                loadUserData()
            },
            onFailure = {
                isSaving = false
                Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClicked: () -> Unit,
    onSaveClicked: (String, String) -> Unit,
    onAvatarClicked: () -> Unit,
    onResetAvatar: () -> Unit, // New callback to reset the selected avatar
    selectedImageUri: Uri?,
    originalAvatarUrl: String,
    originalName: String,
    originalStatus: String,
    isSaving: Boolean,
    googleDisplayName: String,
    googlePhotoUrl: String
) {
    var newName by remember(originalName) { mutableStateOf(TextFieldValue(originalName)) }
    var newStatus by remember(originalStatus) { mutableStateOf(TextFieldValue(originalStatus)) }

    val hasChanges = remember(newName.text, newStatus.text, selectedImageUri, originalName, originalStatus) {
        newName.text.trim() != originalName.trim() || newStatus.text.trim() != originalStatus.trim() || selectedImageUri != null
    }

    val avatarPainter = rememberAsyncImagePainter(
        model = selectedImageUri ?: originalAvatarUrl.ifEmpty { R.drawable.ic_profile_placeholder }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { onAvatarClicked() }
            ) {
                Image(
                    painter = avatarPainter,
                    contentDescription = "Аватар пользователя",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = FirebaseAuth.getInstance().currentUser?.email ?: "Email не задан", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Имя пользователя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newStatus,
                onValueChange = { newStatus = it },
                label = { Text("Статус") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSaveClicked(newName.text, newStatus.text) },
                enabled = hasChanges && !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Сохранение..." else "Сохранить изменения")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    newName = TextFieldValue(googleDisplayName)
                    newStatus = TextFieldValue("")
                    onResetAvatar()
                },
                enabled = hasChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Вернуть стандартные настройки")
            }
        }
    }
}