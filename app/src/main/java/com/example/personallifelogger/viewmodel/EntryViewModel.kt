package com.example.personallifelogger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallifelogger.LifeLoggerApp
import com.example.personallifelogger.data.Entry
import com.example.personallifelogger.data.EntryRepository
import com.example.personallifelogger.sync.FirebaseSync
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EntryRepository =
        (application as LifeLoggerApp).repository

    private var currentUserId: String = ""

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Dynamic entries based on selected category and user
    val categorizedEntries: StateFlow<List<Entry>> = _selectedCategory.flatMapLatest { category ->
        if (currentUserId.isNotEmpty()) {
            repository.getEntriesByUserIdAndCategory(currentUserId, category)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addEntry(title: String, description: String, imageUri: String?, audioUri: String? = null, category: String = "All") {
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                repository.insert(
                    Entry(
                        title = title,
                        description = description,
                        imageUri = imageUri,
                        audioUri = audioUri,
                        category = category,
                        userId = currentUserId
                    )
                )
            }
        }
    }

    fun getEntryById(id: Long): Flow<Entry?> {
        return repository.getEntryById(currentUserId, id)
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    suspend fun getById(id: Long): Entry? = repository.getById(currentUserId, id)

    fun syncToCloud(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            FirebaseSync.uploadEntries(categorizedEntries.value, onResult)
        }
    }
}