package com.example.personallifelogger.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Insert
    suspend fun insert(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT * FROM entries WHERE userId = :userId ORDER BY date DESC")
    fun getEntriesByUserId(userId: String): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getEntriesByUserIdAndCategory(userId: String, category: String): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE userId = :userId AND id = :id")
    fun getEntryById(userId: String, id: Long): Flow<Entry?>

    @Query("SELECT * FROM entries WHERE userId = :userId AND id = :id")
    suspend fun getById(userId: String, id: Long): Entry?

    // Keep these for backward compatibility or remove if not needed
    @Query("SELECT * FROM entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE category = :category ORDER BY date DESC")
    fun getEntriesByCategory(category: String): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getEntryById(id: Long): Flow<Entry?>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): Entry?
}