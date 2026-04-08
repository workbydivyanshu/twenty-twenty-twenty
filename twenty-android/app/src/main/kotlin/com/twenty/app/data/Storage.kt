package com.twenty.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "twenty")

class Storage(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    companion object {
        val SETTINGS_KEY = stringPreferencesKey("settings")
        val SESSIONS_KEY = stringPreferencesKey("sessions")
    }

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        prefs[SETTINGS_KEY]?.let {
            try { json.decodeFromString<Settings>(it) } catch (_: Exception) { Settings() }
        } ?: Settings()
    }

    val sessionsFlow: Flow<List<Session>> = context.dataStore.data.map { prefs ->
        prefs[SESSIONS_KEY]?.let {
            try { json.decodeFromString<List<Session>>(it) } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = json.encodeToString(settings)
        }
    }

    suspend fun saveSessions(sessions: List<Session>) {
        context.dataStore.edit { prefs ->
            prefs[SESSIONS_KEY] = json.encodeToString(sessions)
        }
    }

    fun generateId(): String = java.util.UUID.randomUUID().toString()
}
