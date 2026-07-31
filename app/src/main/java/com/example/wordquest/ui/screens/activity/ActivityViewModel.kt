package com.example.wordquest.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordquest.data.api.WordResponse
import com.example.wordquest.data.local.UserStatEntity
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.QuizMode
import com.example.wordquest.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ActivityUiState(
    val currentWord: WordResponse? = null,
    val isLoading: Boolean = false,
    val isRevealed: Boolean = false,
    val error: String? = null,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val maxQuestions: Int = 10,
    val quizFinished: Boolean = false,
    val quizMode: QuizMode = QuizMode.FLASHCARD,
    val options: List<String> = emptyList(),
    val selectedOption: String? = null,
    val isAnswerCorrect: Boolean? = null
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: WordRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    var uiState by mutableStateOf(ActivityUiState())
        private set

    private val wordPool = listOf(
        "Abundant", "Benevolent", "Candid", "Diligent", "Eloquent",
        "Frugal", "Gregarious", "Humble", "Impartial", "Jovial",
        "Keen", "Lucid", "Meticulous", "Nostalgic", "Obscure",
        "Prudent", "Quaint", "Resilient", "Stoic", "Timid",
        "Aesthetic", "Belligerent", "Capricious", "Defiant", "Ephemeral",
        "Facetious", "Garrulous", "Hypocrisy", "Inherent", "Lethargic",
        "Magnanimous", "Nefarious", "Omnipotent", "Pensive", "Reclusive",
        "Serendipity", "Trepidation", "Ubiquitous", "Venerable", "Wistful",
        "Adversity", "Brevity", "Conundrum", "Dormant", "Exacerbate",
        "Fortuitous", "Gullible", "Hapless", "Indifferent", "Juxtapose",
        "Kindle", "Lament", "Malleable", "Nuance", "Oblivion",
        "Pinnacle", "Quell", "Raucous", "Surmount", "Tactful",
        "Untenable", "Vacillate", "Wary", "Zealot", "Alacrity",
        "Burgeon", "Castigate", "Diatribe", "Elucidate", "Fastidious",
        "Gloat", "Harangue", "Inundate", "Judicious", "Knack",
        "Loquacious", "Mundane", "Nonchalant", "Ominous", "Pervasive",
        "Quandary", "Ruminate", "Skeptical", "Thrifty", "Upbraid",
        "Versatile", "Whet", "Yield", "Zenith", "Appease",
        "Brazen", "Coerce", "Deferent", "Enmity", "Fabricate"
    )
    private var wordsToLearn = emptyList<String>()
    private var currentIndex = 0
    private var sessionJob: Job? = null
    private var lookupJob: Job? = null

    init {
        startNewSession()
    }

    fun startNewSession() {
        sessionJob?.cancel()
        lookupJob?.cancel()
        uiState = ActivityUiState(isLoading = true)
        sessionJob = viewModelScope.launch {
            val goal = settingsManager.dailyGoal.first()
            val mode = settingsManager.quizMode.first()
            wordsToLearn = wordPool.shuffled().take(goal)
            currentIndex = 0
            uiState = ActivityUiState(
                maxQuestions = wordsToLearn.size,
                quizMode = mode
            )
            loadNextWord()
        }
    }

    fun loadNextWord() {
        if (wordsToLearn.isEmpty() || uiState.isLoading || uiState.quizFinished) {
            return
        }

        if (currentIndex >= wordsToLearn.size) {
            finishQuiz()
            return
        }

        val requestedWord = wordsToLearn[currentIndex]
        uiState = uiState.copy(
            isLoading = true,
            currentWord = null,
            isRevealed = false,
            error = null,
            selectedOption = null,
            isAnswerCorrect = null
        )
        lookupJob = viewModelScope.launch {
            repository.getWordDefinition(requestedWord).onSuccess { response ->
                val wordResponse = response.copy(word = requestedWord)
                val options = if (uiState.quizMode == QuizMode.MULTIPLE_CHOICE) {
                    generateOptions(requestedWord)
                } else {
                    emptyList()
                }

                uiState = uiState.copy(
                    currentWord = wordResponse,
                    isLoading = false,
                    totalQuestions = uiState.totalQuestions + 1,
                    options = options
                )
                currentIndex++
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = error.toUserMessage()
                )
            }
        }
    }

    private fun generateOptions(correctWord: String): List<String> {
        val distractors = wordPool
            .filterNot { it.equals(correctWord, ignoreCase = true) }
            .shuffled()
            .take(3)
        return (distractors + correctWord).shuffled()
    }

    fun selectOption(option: String) {
        if (uiState.isLoading || uiState.selectedOption != null) {
            return
        }
        val isCorrect = option.equals(uiState.currentWord?.word, ignoreCase = true)
        uiState = uiState.copy(
            selectedOption = option,
            isAnswerCorrect = isCorrect,
            score = if (isCorrect) uiState.score + 1 else uiState.score
        )
    }

    fun revealMeaning() {
        if (!uiState.isLoading && uiState.currentWord != null) {
            uiState = uiState.copy(isRevealed = true)
        }
    }

    fun submitAnswer(isCorrect: Boolean) {
        if (uiState.isLoading || uiState.quizFinished || uiState.currentWord == null) {
            return
        }
        if (isCorrect) {
            uiState = uiState.copy(score = uiState.score + 1)
        }
        loadNextWord()
    }

    fun skipCurrentWord() {
        if (uiState.isLoading || uiState.error == null) {
            return
        }
        currentIndex++
        loadNextWord()
    }

    private fun finishQuiz() {
        if (uiState.quizFinished) {
            return
        }
        uiState = uiState.copy(quizFinished = true)
        if (uiState.totalQuestions == 0) {
            return
        }
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = dateFormat.format(Date())
            repository.insertStat(
                UserStatEntity(
                    date = date,
                    score = uiState.score,
                    totalQuestions = uiState.totalQuestions
                )
            )
        }
    }

    private fun Throwable.toUserMessage(): String {
        return when (this) {
            is NoSuchElementException -> "No definition was found for this word."
            else -> "Unable to load this word. Check your connection and try again."
        }
    }
}
