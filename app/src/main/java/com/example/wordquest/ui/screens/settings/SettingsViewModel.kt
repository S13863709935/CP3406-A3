package com.example.wordquest.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordquest.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyGoal: Int = 10,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val quizMode: String = "FLASHCARD"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsManager.dailyGoal,
        settingsManager.isDarkMode,
        settingsManager.quizMode,
        settingsManager.notificationsEnabled
    ) { dailyGoal, isDarkMode, quizMode, notificationsEnabled ->
        SettingsUiState(
            dailyGoal = dailyGoal,
            isDarkMode = isDarkMode,
            quizMode = quizMode,
            notificationsEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            settingsManager.setDailyGoal(goal)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(enabled)
        }
    }

    fun updateQuizMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setQuizMode(mode)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(enabled)
        }
    }
}
