package com.example.wordquest.data.api

import retrofit2.http.GET
import retrofit2.http.Path

data class WordResponse(
    val word: String,
    val meanings: List<Meaning> = emptyList(),
    val phonetic: String? = null
)

data class Meaning(
    val partOfSpeech: String = "",
    val definitions: List<Definition> = emptyList()
)

data class Definition(
    val definition: String,
    val example: String? = null
)

interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun getWordDefinition(@Path("word") word: String): List<WordResponse>
}
