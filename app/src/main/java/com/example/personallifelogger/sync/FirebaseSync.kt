package com.example.personallifelogger.sync

import com.example.personallifelogger.data.Entry
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Simple wrapper around Firestore for manual cloud sync.
 * Requires a valid google-services.json placed in app/.
 */
object FirebaseSync {
    private const val COLLECTION = "entries"

    fun uploadEntries(entries: List<Entry>, onResult: (Boolean, String) -> Unit) {
        if (entries.isEmpty()) {
            onResult(true, "Nothing to sync")
            return
        }
        try {
            val db = Firebase.firestore
            val batch = db.batch()
            entries.forEach { entry ->
                val ref = db.collection(COLLECTION).document(entry.id.toString())
                batch.set(
                    ref,
                    mapOf(
                        "id" to entry.id,
                        "title" to entry.title,
                        "description" to entry.description,
                        "imageUri" to entry.imageUri,
                        "date" to entry.date
                    )
                )
            }
            batch.commit()
                .addOnSuccessListener { onResult(true, "Synced ${entries.size} entries") }
                .addOnFailureListener { onResult(false, it.message ?: "Sync failed") }
        } catch (e: Exception) {
            onResult(false, e.message ?: "Firebase not configured")
        }
    }
}
