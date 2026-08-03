package com.example.wordquest.ui.screens.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordquest.data.local.UserStatEntity
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.SettingsManager
import com.example.wordquest.ui.model.ProgressSummary
import com.example.wordquest.ui.model.ProgressSummaryCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class LandingUiState(
    val dailyGoal: Int = SettingsManager.DEFAULT_DAILY_GOAL,
    val summary: ProgressSummary = ProgressSummary(),
    val recentStats: List<UserStatEntity> = emptyList()
) {
    companion object {
        fun from(
            stats: List<UserStatEntity>,
            dailyGoal: Int,
            today: String
        ): LandingUiState {
            return LandingUiState(
                dailyGoal = dailyGoal,
                summary = ProgressSummaryCalculator.calculate(stats, today),
                recentStats = stats.take(5)
            )
        }
    }
}

@HiltViewModel
class LandingViewModel @Inject constructor(
    repository: WordRepository,
    settingsManager: SettingsManager
) : ViewModel() {
    val uiState: StateFlow<LandingUiState> = combine(
        repository.getAllStats(),
        settingsManager.dailyGoal
    ) { stats, dailyGoal ->
        LandingUiState.from(
            stats = stats,
            dailyGoal = dailyGoal,
            today = LocalDate.now().toString()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LandingUiState()
    )
}
