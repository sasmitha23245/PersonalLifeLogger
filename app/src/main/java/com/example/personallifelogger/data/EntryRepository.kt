package com.example.personallifelogger.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntryRepository private constructor(private val entryDao: EntryDao) {

    fun getEntriesByUserId(userId: String): Flow<List<Entry>> {
        return entryDao.getEntriesByUserId(userId)
    }

    fun getEntriesByUserIdAndCategory(userId: String, category: String): Flow<List<Entry>> {
        return if (category == "All") {
            entryDao.getEntriesByUserId(userId)
        } else {
            entryDao.getEntriesByUserIdAndCategory(userId, category)
        }
    }

    suspend fun insert(entry: Entry) {
        entryDao.insert(entry)
    }

    suspend fun delete(entry: Entry) {
        entryDao.delete(entry)
    }

    fun getEntryById(userId: String, id: Long): Flow<Entry?> {
        return entryDao.getEntryById(userId, id)
    }

    suspend fun getById(userId: String, id: Long): Entry? {
        return entryDao.getById(userId, id)
    }

    companion object {
        @Volatile
        private var instance: EntryRepository? = null

        fun getRepository(context: Context): EntryRepository {
            return instance ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                EntryRepository(db.entryDao()).also { instance = it }
            }
        }
    }
}