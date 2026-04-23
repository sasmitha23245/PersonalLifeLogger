package com.example.personallifelogger.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.personallifelogger.data.Category
import com.example.personallifelogger.data.Entry
import com.example.personallifelogger.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EntryViewModel,
    onAddClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onSignOut: () -> Unit
) {
    val categorizedEntries by viewModel.categorizedEntries.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Filter entries based on search query
    val filteredEntries = remember(categorizedEntries, searchQuery) {
        if (searchQuery.isBlank()) {
            categorizedEntries
        } else {
            categorizedEntries.filter { entry ->
                entry.title.contains(searchQuery, ignoreCase = true) ||
                        entry.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search entries...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(
                            "My Life Log",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (isSearching) {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!isSearching) {
                        // Sign Out button
                        IconButton(onClick = onSignOut) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                        }
                        // Search icon
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        // Sync icon
                        IconButton(onClick = {
                            viewModel.syncToCloud { _, _ ->
                                // Sync callback
                            }
                        }) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Sync")
                        }
                    } else {
                        // Clear search button
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add entry")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Only show tabs when not searching
            if (!isSearching) {
                // Category Tabs
                ScrollableTabRow(
                    selectedTabIndex = Category.values.indexOfFirst { it.title == selectedCategory },
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    Category.values.forEach { category ->
                        Tab(
                            selected = selectedCategory == category.title,
                            onClick = { viewModel.selectCategory(category.title) },
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category.icon, fontSize = 14.sp)
                                    Text(
                                        category.title,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                // Search results header
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Search results for \"$searchQuery\" (${filteredEntries.size})",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Entries List
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching entries found" else "No entries in ${selectedCategory}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Tap + to add your first memory",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        EntryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) },
                            onDelete = {
                                viewModel.deleteEntry(entry)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EntryCard(entry: Entry, onClick: () -> Unit, onDelete: () -> Unit) {
    val df = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(12.dp)
            ) {
                // Title and Category Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = getCategoryIcon(entry.category),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(df.format(Date(entry.date)), style = MaterialTheme.typography.labelSmall)

                if (entry.description.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        entry.description.take(100) + if (entry.description.length > 100) "…" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        fontSize = 13.sp
                    )
                }

                // Media indicators
                if (entry.imageUri != null || entry.audioUri != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (entry.imageUri != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Has image",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " Photo",
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }
                        }
                        if (entry.audioUri != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Audiotrack,
                                    contentDescription = "Has audio",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = " Audio",
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }
                        }
                    }
                }
            }

            // Image thumbnail and delete button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // Image thumbnail if exists
                if (entry.imageUri != null) {
                    AsyncImage(
                        model = Uri.parse(entry.imageUri),
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): String {
    return when (category) {
        "All" -> "📋"
        "Daily Life" -> "🏠"
        "Health & Fitness" -> "💪"
        "Education" -> "📚"
        "Work & Productivity" -> "💼"
        "Social & Fun" -> "🎉"
        "Mental & Reflection" -> "🧠"
        "Finance" -> "💰"
        "Other" -> "📌"
        else -> "📌"
    }
}