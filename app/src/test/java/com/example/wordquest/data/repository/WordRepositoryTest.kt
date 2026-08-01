package com.example.wordquest.data.repository

import com.example.wordquest.data.api.DictionaryApiService
import com.example.wordquest.data.api.WordResponse
import com.example.wordquest.data.local.WordDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordRepositoryTest {

    private val apiService = mockk<DictionaryApiService>()
    private val wordDao = mockk<WordDao>(relaxed = true)
    private val repository = WordRepository(apiService, wordDao)

    @Test
    fun `lookup normalizes input and returns first definition`() = runTest {
        val expected = WordResponse(word = "abundant")
        coEvery { apiService.getWordDefinition("abundant") } returns listOf(expected)

        val result = repository.getWordDefinition("  Abundant ")

        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { apiService.getWordDefinition("abundant") }
    }

    @Test
    fun `blank lookup fails without calling api`() = runTest {
        val result = repository.getWordDefinition("   ")

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { apiService.getWordDefinition(any()) }
    }

    @Test
    fun `empty api response returns a useful failure`() = runTest {
        coEvery { apiService.getWordDefinition("unknown") } returns emptyList()

        val result = repository.getWordDefinition("unknown")

        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `lookup does not swallow coroutine cancellation`() = runTest {
        coEvery { apiService.getWordDefinition("abundant") } throws CancellationException()

        var cancellationWasThrown = false
        try {
            repository.getWordDefinition("abundant")
        } catch (_: CancellationException) {
            cancellationWasThrown = true
        }

        assertTrue(cancellationWasThrown)
    }

    @Test
    fun `clear stats removes all stored history`() = runTest {
        repository.clearStats()

        coVerify(exactly = 1) { wordDao.deleteAllStats() }
    }
}
