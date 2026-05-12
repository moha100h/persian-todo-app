package ir.moha.persiantodo.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "settings")
val DARK_MODE_KEY     = booleanPreferencesKey("dark_mode")
val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
