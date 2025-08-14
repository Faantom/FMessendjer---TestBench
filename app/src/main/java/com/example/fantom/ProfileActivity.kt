package com.example.fantom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var ivAvatar: ImageView
    private lateinit var etUserName: EditText
    private lateinit var tvUserEmail: TextView
    private lateinit var etUserStatus: EditText
    private lateinit var btnSave: Button
    private lateinit var btnRestoreDefaults: Button

    private var originalName: String = ""
    private var originalStatus: String = ""
    private var originalAvatarUrl: String = ""

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    Glide.with(this).load(uri).apply(RequestOptions.circleCropTransform()).into(ivAvatar)
                    checkChanges()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userRepository = UserRepository(this)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val toolbar: Toolbar = findViewById(R.id.profile_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Профиль"

        ivAvatar = findViewById(R.id.ivAvatar)
        etUserName = findViewById(R.id.etUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        etUserStatus = findViewById(R.id.etUserStatus)
        btnSave = findViewById(R.id.btnSave)
        btnRestoreDefaults = findViewById(R.id.btnRestoreDefaults)

        loadUserData()
        setupListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        // Отслеживание изменений в полях ввода
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkChanges()
            }
        }
        etUserName.addTextChangedListener(textWatcher)
        etUserStatus.addTextChangedListener(textWatcher)

        ivAvatar.setOnClickListener {
            checkAndRequestPermissions()
        }

        btnSave.setOnClickListener {
            saveUserData()
        }

        // Обработчик нажатия на кнопку "Вернуть стандартные настройки"
        btnRestoreDefaults.setOnClickListener {
            restoreDefaults()
        }
    }

    private fun checkChanges() {
        val nameChanged = etUserName.text.toString().trim() != originalName
        val statusChanged = etUserStatus.text.toString().trim() != originalStatus
        val avatarChanged = selectedImageUri != null

        btnSave.isEnabled = nameChanged || statusChanged || avatarChanged
    }

    private fun loadUserData() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            originalName = firebaseUser.displayName ?: ""
            etUserName.setText(originalName)
            tvUserEmail.text = firebaseUser.email ?: "Email не задан"

            userRepository.loadUser(firebaseUser.uid, object : UserRepository.UserCallback {
                override fun onUserLoaded(user: User?) {
                    if (user != null) {
                        originalStatus = user.status
                        originalAvatarUrl = user.avatarUrl
                        etUserStatus.setText(originalStatus)
                        if (originalAvatarUrl.isNotEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(originalAvatarUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .into(ivAvatar)
                        } else {
                            Glide.with(this@ProfileActivity)
                                .load(R.drawable.ic_profile_placeholder)
                                .apply(RequestOptions.circleCropTransform())
                                .into(ivAvatar)
                        }
                    }
                }
                override fun onError(e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun restoreDefaults() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            etUserName.setText(firebaseUser.displayName ?: "")
            etUserStatus.setText("")
            selectedImageUri = null
            firebaseUser.photoUrl?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar)
            } ?: run {
                Glide.with(this)
                    .load(R.drawable.ic_profile_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar)
            }
            checkChanges()
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveUserData() {
        val user = auth.currentUser
        if (user != null) {
            val newName = etUserName.text.toString().trim()
            val newStatus = etUserStatus.text.toString().trim()

            if (selectedImageUri != null) {
                uploadImageAndSaveData(user, newName, newStatus)
            } else {
                updateUserProfile(user, newName, newStatus, user.photoUrl.toString())
            }
        }
    }

    private fun uploadImageAndSaveData(user: com.google.firebase.auth.FirebaseUser, newName: String, newStatus: String) {
        val storageRef = storage.reference.child("avatars/${user.uid}")
        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val avatarUrl = downloadUri.toString()
                        updateUserProfile(user, newName, newStatus, avatarUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ошибка загрузки аватара", Toast.LENGTH_SHORT).show()
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
                loadUserData()
                btnSave.isEnabled = false
            },
            onFailure = {
                Toast.makeText(this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show()
            }
        )
    }
}