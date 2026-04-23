package com.example.personallifelogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single life log entry.
 */
@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",  // Add this field for user-specific entries
    val title: String,
    val description: String,
    val imageUri: String? = null,
    val audioUri: String? = null,
    val category: String = "All",
    val date: Long = System.currentTimeMillis()
)