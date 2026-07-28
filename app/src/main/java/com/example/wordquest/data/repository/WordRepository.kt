package com.example.wordquest.data.repository

import com.example.wordquest.data.api.DictionaryApiService
import com.example.wordquest.data.api.WordResponse
import com.example.wordquest.data.local.UserStatEntity
import com.example.wordquest.data.local.WordDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val apiService: DictionaryApiService,
    private val wordDao: WordDao
) {
    suspend fun getWordDefinition(word: String): Result<List<WordResponse>> {
        return try {
            val response = apiService.getWordDefinition(word)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllStats(): Flow<List<UserStatEntity>> = wordDao.getAllStats()

    suspend fun insertStat(stat: UserStatEntity) {
        wordDao.insertStat(stat)
    }
}
