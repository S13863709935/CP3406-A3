package com.example.wordquest.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StatsUiState(
    val dailyGoal: Int = 10,
    val learnedToday: Int = 0,
    val statsList: List<com.example.wordquest.data.local.UserStatEntity> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: WordRepository,
    settingsManager: SettingsManager
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllStats(),
        settingsManager.dailyGoal
    ) { stats, goal ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val learnedToday = stats
            .filter { it.date.startsWith(today) }
            .sumOf { it.totalQuestions }

        StatsUiState(
            dailyGoal = goal,
            learnedToday = learnedToday,
            statsList = stats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )
}
