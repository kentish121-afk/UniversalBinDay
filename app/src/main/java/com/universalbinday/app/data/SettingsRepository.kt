package com.universalbinday.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.universalbinday.app.model.AppSettings
import com.universalbinday.app.model.Defaults
import com.universalbinday.app.model.UserBinConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val gson = Gson()
    private val SETTINGS_KEY = stringPreferencesKey("app_settings")

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val json = prefs[SETTINGS_KEY]
        if (json.isNullOrBlank()) {
            AppSettings(bins = Defaults.defaultUserBins())
        } else {
            try {
                gson.fromJson(json, AppSettings::class.java)
            } catch (e: Exception) {
                AppSettings(bins = Defaults.defaultUserBins())
            }
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = gson.toJson(settings)
        }
    }

    suspend fun updateBin(config: UserBinConfig) {
        context.dataStore.edit { prefs ->
            val current = prefs[SETTINGS_KEY]?.let {
                try { gson.fromJson(it, AppSettings::class.java) } catch (_: Exception) { null }
            } ?: AppSettings(bins = Defaults.defaultUserBins())

            val newBins = current.bins.map {
                if (it.binId == config.binId) config else it
            }.let { list ->
                if (list.none { it.binId == config.binId }) list + config else list
            }

            prefs[SETTINGS_KEY] = gson.toJson(current.copy(bins = newBins))
        }
    }
}
