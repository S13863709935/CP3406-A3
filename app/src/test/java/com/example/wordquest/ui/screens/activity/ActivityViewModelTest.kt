package com.example.wordquest.ui.screens.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.QuizMode
import com.example.wordquest.data.settings.SettingsManager
import com.example.wordquest.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: WordRepository
    private lateinit var settingsManager: SettingsManager
    private lateinit var viewModel: ActivityViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        settingsManager = mockk(relaxed = true)
    }

    @Test
    fun `init should load correct number of words from settings`() = runTest {
        // Given
        val expectedGoal = 5
        every { settingsManager.dailyGoal } returns flowOf(expectedGoal)
        every { settingsManager.quizMode } returns flowOf(QuizMode.FLASHCARD)
        coEvery { repository.getWordDefinition(any()) } returns Result.success(emptyList())

        // When
        viewModel = ActivityViewModel(repository, settingsManager)
        advanceUntilIdle()

        // Then
        assertEquals(expectedGoal, viewModel.uiState.maxQuestions)
    }

    @Test
    fun `submitAnswer should increment score when correct`() = runTest {
        // Given
        val goal = 10
        every { settingsManager.dailyGoal } returns flowOf(goal)
        every { settingsManager.quizMode } returns flowOf(QuizMode.FLASHCARD)
        coEvery { repository.getWordDefinition(any()) } returns Result.success(emptyList())

        viewModel = ActivityViewModel(repository, settingsManager)
        advanceUntilIdle()

        val initialScore = viewModel.uiState.score

        // When
        viewModel.submitAnswer(true)

        // Then
        assertEquals(initialScore + 1, viewModel.uiState.score)
    }
}
