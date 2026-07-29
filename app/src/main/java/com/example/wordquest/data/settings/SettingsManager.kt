package com.example.wordquest.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.DARK_MODE] ?: false
    }

    val dailyGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        (preferences[Keys.DAILY_GOAL] ?: DEFAULT_DAILY_GOAL)
            .coerceIn(MIN_DAILY_GOAL, MAX_DAILY_GOAL)
    }

    val quizMode: Flow<QuizMode> = context.dataStore.data.map { preferences ->
        QuizMode.fromStoredValue(preferences[Keys.QUIZ_MODE])
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DAILY_GOAL] = goal.coerceIn(MIN_DAILY_GOAL, MAX_DAILY_GOAL)
        }
    }

    suspend fun setQuizMode(mode: QuizMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.QUIZ_MODE] = mode.name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val QUIZ_MODE = stringPreferencesKey("quiz_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    companion object {
        const val DEFAULT_DAILY_GOAL = 10
        const val MIN_DAILY_GOAL = 5
        const val MAX_DAILY_GOAL = 50
    }
}
