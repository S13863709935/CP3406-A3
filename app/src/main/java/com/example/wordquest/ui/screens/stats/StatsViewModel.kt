package com.example.wordquest.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordquest.data.local.UserStatEntity
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StatsUiState(
    val dailyGoal: Int = 10,
    val statsList: List<UserStatEntity> = emptyList(),
    val summary: StatsSummary = StatsSummary()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: WordRepository,
    settingsManager: SettingsManager
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllStats(),
        settingsManager.dailyGoal
    ) { stats, goal ->
        StatsUiState(
            dailyGoal = goal,
            statsList = stats,
            summary = StatsSummaryCalculator.calculate(
                stats = stats,
                today = LocalDate.now().toString()
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearStats()
        }
    }
}
