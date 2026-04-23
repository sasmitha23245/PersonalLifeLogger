package com.example.personallifelogger.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.example.personallifelogger.data.Entry
import com.example.personallifelogger.viewmodel.EntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryId: Long,
    viewModel: EntryViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val entry by viewModel.getEntryById(entryId).collectAsStateWithLifecycle(initialValue = null)
    var showImageDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    // Initialize ExoPlayer for audio playback
    val exoPlayer = remember(entry) {
        ExoPlayer.Builder(context).build().apply {
            entry?.audioUri?.let { audioUri ->
                try {
                    val mediaItem = MediaItem.fromUri(Uri.parse(audioUri))
                    setMediaItem(mediaItem)
                    prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Release player when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Update isPlaying state
    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Show loading or error state
    if (entry == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentEntry = entry!!

    // Image preview dialog
    if (showImageDialog && currentEntry.imageUri != null) {
        Dialog(
            onDismissRequest = { showImageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showImageDialog = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = Uri.parse(currentEntry.imageUri),
                    contentDescription = "Full size image",
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { showImageDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentEntry.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteEntry(currentEntry)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "📅 ${android.text.format.DateFormat.format("MMMM dd, yyyy 'at' hh:mm a", currentEntry.date)}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = currentEntry.description.ifEmpty { "No description provided" },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Image display with view button
            currentEntry.imageUri?.let { uriString ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Image",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Attached Image",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Button(
                                onClick = { showImageDialog = true },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View", fontSize = MaterialTheme.typography.labelLarge.fontSize)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = Uri.parse(uriString),
                            contentDescription = "Entry image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showImageDialog = true }
                        )
                    }
                }
            }

            // Audio player with enhanced controls
            currentEntry.audioUri?.let { audioUri ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Audiotrack,
                                contentDescription = "Audio",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audio Recording",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Enhanced audio controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { exoPlayer.seekTo(0); exoPlayer.play() }) {
                                Icon(Icons.Default.Replay, contentDescription = "Restart", modifier = Modifier.size(24.dp))
                            }

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(onClick = { exoPlayer.stop() }) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}