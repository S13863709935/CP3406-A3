package com.example.wordquest.ui.screens.settings

import com.example.wordquest.data.settings.QuizMode
import com.example.wordquest.data.settings.SettingsManager
import com.example.wordquest.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsManager: SettingsManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        settingsManager = mockk(relaxed = true)
        every { settingsManager.dailyGoal } returns flowOf(10)
        every { settingsManager.isDarkMode } returns flowOf(false)
        every { settingsManager.quizMode } returns flowOf(QuizMode.FLASHCARD)
        every { settingsManager.notificationsEnabled } returns flowOf(true)
        viewModel = SettingsViewModel(settingsManager)
    }

    @Test
    fun `daily goal changes are persisted`() = runTest {
        viewModel.updateDailyGoal(25)
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsManager.setDailyGoal(25) }
    }

    @Test
    fun `dark mode changes are persisted`() = runTest {
        viewModel.toggleDarkMode(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsManager.setDarkMode(true) }
    }

    @Test
    fun `quiz mode changes are persisted`() = runTest {
        viewModel.updateQuizMode(QuizMode.MULTIPLE_CHOICE)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            settingsManager.setQuizMode(QuizMode.MULTIPLE_CHOICE)
        }
    }

    @Test
    fun `notification changes are persisted`() = runTest {
        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsManager.setNotificationsEnabled(false) }
    }
}
