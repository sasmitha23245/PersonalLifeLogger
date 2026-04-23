package com.example.personallifelogger.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.personallifelogger.data.Category
import com.example.personallifelogger.viewmodel.EntryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: EntryViewModel,
    onSaved: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Determine if screen is small (less than 360dp width)
    val isSmallScreen = screenWidth < 360.dp

    val photoFile = remember {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir = context.cacheDir
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            null
        }
    }

    val cameraUri = photoFile?.let { file ->
        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            errorMessage = "File provider error: ${e.message}"
            null
        }
    }

    fun hasCameraPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun hasAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) errorMessage = "Camera permission needed"
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) errorMessage = "Microphone permission needed"
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            errorMessage = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PersonalLifeLogger")
                    }
                }
                val savedUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                savedUri?.let {
                    context.contentResolver.openInputStream(cameraUri)?.use { input ->
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    imageUri = it
                } ?: run {
                    imageUri = cameraUri
                }
                photoFile?.delete()
            } catch (e: Exception) {
                errorMessage = "Error saving photo: ${e.message}"
            }
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) { }
                    audioUri = uri
                    errorMessage = null
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    fun takePhoto() {
        if (!hasCameraPermission()) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        cameraUri?.let { cameraLauncher.launch(it) }
            ?: run { errorMessage = "Camera error" }
    }

    fun recordAudio() {
        if (!hasAudioPermission()) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val intent = android.content.Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            if (intent.resolveActivity(context.packageManager) != null) {
                audioLauncher.launch(intent)
            } else {
                errorMessage = "No voice recorder found"
            }
        } catch (e: Exception) {
            errorMessage = "Cannot open recorder: ${e.message}"
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("New Entry") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Category.values.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category.icon, fontSize = 16.sp)
                                    Text(category.title)
                                }
                            },
                            onClick = {
                                selectedCategory = category.title
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Responsive buttons - adapts to screen size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
            ) {
                // Gallery Button
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSmallScreen) 48.dp else 56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                    )
                    if (!isSmallScreen) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gallery",
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                // Camera Button
                Button(
                    onClick = { takePhoto() },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSmallScreen) 48.dp else 56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                    )
                    if (!isSmallScreen) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Camera",
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                // Audio Button
                Button(
                    onClick = { recordAudio() },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSmallScreen) 48.dp else 56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Audio",
                        modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                    )
                    if (!isSmallScreen) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Audio",
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = it, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }

            imageUri?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                        )
                        TextButton(onClick = { imageUri = null }, modifier = Modifier.align(Alignment.End)) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }

            audioUri?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Audio recorded", fontSize = 13.sp)
                        }
                        TextButton(onClick = { audioUri = null }) {
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addEntry(
                            title.trim(),
                            description.trim(),
                            imageUri?.toString(),
                            audioUri?.toString(),
                            selectedCategory
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 48.dp else 56.dp),
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Entry", fontSize = if (isSmallScreen) 14.sp else 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}