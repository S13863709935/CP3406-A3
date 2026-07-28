package com.example.wordquest.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordquest.data.api.WordResponse
import com.example.wordquest.data.local.UserStatEntity
import com.example.wordquest.data.repository.WordRepository
import com.example.wordquest.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    val quizMode: String = "FLASHCARD",
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

    init {
        viewModelScope.launch {
            val goal = settingsManager.dailyGoal.first()
            val mode = settingsManager.quizMode.first()
            wordsToLearn = wordPool.shuffled().take(goal)
            uiState = uiState.copy(maxQuestions = wordsToLearn.size, quizMode = mode)
            loadNextWord()
        }
    }

    fun loadNextWord() {
        if (wordsToLearn.isEmpty()) return // Wait for init

        if (currentIndex >= wordsToLearn.size) {
            finishQuiz()
            return
        }

        uiState = uiState.copy(
            isLoading = true,
            isRevealed = false,
            error = null,
            selectedOption = null,
            isAnswerCorrect = null
        )
        viewModelScope.launch {
            repository.getWordDefinition(wordsToLearn[currentIndex]).onSuccess {
                val wordResponse = it.firstOrNull()
                val options = if (uiState.quizMode == "MULTIPLE_CHOICE" && wordResponse != null) {
                    generateOptions(wordResponse.word)
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
            }.onFailure {
                uiState = uiState.copy(isLoading = false, error = it.message)
            }
        }
    }

    private fun generateOptions(correctWord: String): List<String> {
        return (wordPool.filter { it != correctWord }.shuffled().take(3) + correctWord).shuffled()
    }

    fun selectOption(option: String) {
        if (uiState.selectedOption != null) return
        val isCorrect = option == uiState.currentWord?.word
        uiState = uiState.copy(
            selectedOption = option,
            isAnswerCorrect = isCorrect,
            score = if (isCorrect) uiState.score + 1 else uiState.score
        )
    }

    fun revealMeaning() {
        uiState = uiState.copy(isRevealed = true)
    }

    fun submitAnswer(isCorrect: Boolean) {
        if (isCorrect) {
            uiState = uiState.copy(score = uiState.score + 1)
        }
        loadNextWord()
    }

    private fun finishQuiz() {
        uiState = uiState.copy(quizFinished = true)
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
}
