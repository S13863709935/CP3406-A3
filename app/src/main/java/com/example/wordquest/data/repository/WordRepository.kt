package com.example.wordquest.data.repository

import com.example.wordquest.data.api.DictionaryApiService
import com.example.wordquest.data.api.WordResponse
import com.example.wordquest.data.local.UserStatEntity
import com.example.wordquest.data.local.WordDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val apiService: DictionaryApiService,
    private val wordDao: WordDao
) {
    suspend fun getWordDefinition(word: String): Result<WordResponse> {
        val normalizedWord = word.trim().lowercase(Locale.US)
        if (normalizedWord.isBlank()) {
            return Result.failure(IllegalArgumentException("Word cannot be blank"))
        }

        return try {
            val response = apiService.getWordDefinition(normalizedWord).firstOrNull()
                ?: return Result.failure(NoSuchElementException("No definition found for $word"))
            Result.success(response)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    fun getAllStats(): Flow<List<UserStatEntity>> = wordDao.getAllStats()

    suspend fun insertStat(stat: UserStatEntity) {
        wordDao.insertStat(stat)
    }

    suspend fun clearStats() {
        wordDao.deleteAllStats()
    }
}
