package com.example.wordquest.ui.screens.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordquest.data.api.WordResponse
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.QuizMode
import com.example.wordquest.data.settings.SettingsManager
import com.example.wordquest.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    private fun stubSettings(
        goal: Int = 5,
        mode: QuizMode = QuizMode.FLASHCARD
    ) {
        every { settingsManager.dailyGoal } returns flowOf(goal)
        every { settingsManager.quizMode } returns flowOf(mode)
    }

    private fun stubSuccessfulLookups() {
        coEvery { repository.getWordDefinition(any()) } returns Result.success(
            WordResponse(word = "test")
        )
    }

    @Test
    fun `init should load correct number of words from settings`() = runTest {
        // Given
        val expectedGoal = 5
        stubSettings(goal = expectedGoal)
        stubSuccessfulLookups()

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
        stubSettings(goal = goal)
        stubSuccessfulLookups()

        viewModel = ActivityViewModel(repository, settingsManager)
        advanceUntilIdle()

        val initialScore = viewModel.uiState.score

        // When
        viewModel.submitAnswer(true)

        // Then
        assertEquals(initialScore + 1, viewModel.uiState.score)
    }

    @Test
    fun `repeated answer taps only count once while next word loads`() = runTest {
        stubSettings(goal = 2)
        val releaseSecondLookup = CompletableDeferred<Unit>()
        var lookupCount = 0
        coEvery { repository.getWordDefinition(any()) } coAnswers {
            lookupCount++
            if (lookupCount > 1) {
                releaseSecondLookup.await()
            }
            Result.success(WordResponse(word = "test"))
        }
        viewModel = ActivityViewModel(repository, settingsManager)

        viewModel.submitAnswer(true)
        viewModel.submitAnswer(true)

        assertEquals(1, viewModel.uiState.score)
        assertTrue(viewModel.uiState.isLoading)
        releaseSecondLookup.complete(Unit)
    }

    @Test
    fun `failed word can be skipped without increasing question count`() = runTest {
        stubSettings(goal = 2)
        var lookupCount = 0
        coEvery { repository.getWordDefinition(any()) } coAnswers {
            lookupCount++
            if (lookupCount == 1) {
                Result.failure(IllegalStateException("network unavailable"))
            } else {
                Result.success(WordResponse(word = "test"))
            }
        }
        viewModel = ActivityViewModel(repository, settingsManager)

        assertNotNull(viewModel.uiState.error)
        viewModel.skipCurrentWord()

        assertNull(viewModel.uiState.error)
        assertNotNull(viewModel.uiState.currentWord)
        assertEquals(1, viewModel.uiState.totalQuestions)
    }

    @Test
    fun `starting again resets score and creates a fresh session`() = runTest {
        stubSettings(goal = 1)
        stubSuccessfulLookups()
        viewModel = ActivityViewModel(repository, settingsManager)

        viewModel.submitAnswer(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.quizFinished)
        coVerify(exactly = 1) { repository.insertStat(any()) }

        viewModel.startNewSession()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.quizFinished)
        assertEquals(0, viewModel.uiState.score)
        assertEquals(1, viewModel.uiState.totalQuestions)
    }

    @Test
    fun `multiple choice options do not duplicate the answer with different casing`() = runTest {
        stubSettings(goal = 1, mode = QuizMode.MULTIPLE_CHOICE)
        coEvery { repository.getWordDefinition(any()) } returns Result.success(
            WordResponse(word = "lowercase response")
        )

        viewModel = ActivityViewModel(repository, settingsManager)

        val normalizedOptions = viewModel.uiState.options.map { it.lowercase() }
        assertEquals(4, normalizedOptions.size)
        assertEquals(normalizedOptions.size, normalizedOptions.distinct().size)
        assertTrue(
            viewModel.uiState.options.any {
                it.equals(viewModel.uiState.currentWord?.word, ignoreCase = true)
            }
        )
    }

    @Test
    fun `missing definition displays a friendly error`() = runTest {
        stubSettings(goal = 1)
        coEvery { repository.getWordDefinition(any()) } returns Result.failure(
            NoSuchElementException("missing definition")
        )

        viewModel = ActivityViewModel(repository, settingsManager)

        assertEquals(
            "No definition was found for this word.",
            viewModel.uiState.error
        )
    }

    @Test
    fun `skipping every failed word does not save an empty result`() = runTest {
        stubSettings(goal = 1)
        coEvery { repository.getWordDefinition(any()) } returns Result.failure(
            IllegalStateException("network unavailable")
        )
        viewModel = ActivityViewModel(repository, settingsManager)

        viewModel.skipCurrentWord()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.quizFinished)
        assertEquals(0, viewModel.uiState.totalQuestions)
        coVerify(exactly = 0) { repository.insertStat(any()) }
    }
}
