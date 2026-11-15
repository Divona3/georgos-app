package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.grievousDataStore: DataStore<Preferences> by preferencesDataStore(name = "grievous_prefs")

class PreferencesRepository(private val context: Context) {
    private val keyLanguage = stringPreferencesKey("language_code")
    private val keyVaultSavedLeaves = intPreferencesKey("vault_saved_leaves")
    private val keyAutoVoice = booleanPreferencesKey("auto_voice_guide")
    private val keyDarkMode = booleanPreferencesKey("dark_mode_enabled")

    val languageCode: Flow<String> = context.grievousDataStore.data.map { prefs ->
        prefs[keyLanguage] ?: "en"
    }

    val vaultSavedLeaves: Flow<Int> = context.grievousDataStore.data.map { prefs ->
        prefs[keyVaultSavedLeaves] ?: 60
    }

    val autoVoiceEnabled: Flow<Boolean> = context.grievousDataStore.data.map { prefs ->
        prefs[keyAutoVoice] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.grievousDataStore.data.map { prefs ->
        prefs[keyDarkMode] ?: false
    }

    suspend fun setLanguageCode(code: String) {
        context.grievousDataStore.edit { prefs ->
            prefs[keyLanguage] = code
        }
    }

    suspend fun setVaultSavedLeaves(amount: Int) {
        context.grievousDataStore.edit { prefs ->
            prefs[keyVaultSavedLeaves] = amount.coerceAtLeast(0)
        }
    }

    suspend fun setAutoVoiceEnabled(enabled: Boolean) {
        context.grievousDataStore.edit { prefs ->
            prefs[keyAutoVoice] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.grievousDataStore.edit { prefs ->
            prefs[keyDarkMode] = enabled
        }
    }
}
