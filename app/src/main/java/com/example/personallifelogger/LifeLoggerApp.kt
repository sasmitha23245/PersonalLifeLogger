package com.example.personallifelogger

import android.app.Application
import com.example.personallifelogger.data.EntryRepository
import com.google.firebase.FirebaseApp

class LifeLoggerApp : Application() {
    lateinit var repository: EntryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        repository = EntryRepository.getRepository(this)
    }
}