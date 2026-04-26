package com.wenha.cefrenglish.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.userPrefs by preferencesDataStore(name = "user_prefs")

class UserPrefsStore(private val context: Context) {
    private val userIdKey = stringPreferencesKey("user_id")

    suspend fun getOrCreateUserId(): String {
        val existing = context.userPrefs.data.map { it[userIdKey] }.first()
        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        context.userPrefs.edit { it[userIdKey] = newId }
        return newId
    }
}
