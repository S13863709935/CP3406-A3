package com.example.wordquest.ui.model

import com.example.wordquest.data.local.UserStatEntity
import kotlin.math.roundToInt

data class ProgressSummary(
    val totalQuizzes: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val averageAccuracy: Int = 0,
    val bestAccuracy: Int = 0,
    val practicedToday: Int = 0,
    val recentAccuracies: List<Int> = emptyList()
)

object ProgressSummaryCalculator {
    fun calculate(
        stats: List<UserStatEntity>,
        today: String
    ): ProgressSummary {
        val totalQuestions = stats.sumOf { it.validQuestionCount }
        val correctAnswers = stats.sumOf { it.validScore }
        val averageAccuracy = if (totalQuestions == 0) {
            0
        } else {
            (correctAnswers.toDouble() / totalQuestions * 100).roundToInt()
        }

        return ProgressSummary(
            totalQuizzes = stats.size,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            averageAccuracy = averageAccuracy,
            bestAccuracy = stats.maxOfOrNull { it.accuracyPercentage } ?: 0,
            practicedToday = stats
                .filter { it.date.startsWith(today) }
                .sumOf { it.validQuestionCount },
            recentAccuracies = stats
                .take(7)
                .asReversed()
                .map { it.accuracyPercentage }
        )
    }

    private val UserStatEntity.validQuestionCount: Int
        get() = totalQuestions.coerceAtLeast(0)

    private val UserStatEntity.validScore: Int
        get() = score.coerceIn(0, validQuestionCount)

    private val UserStatEntity.accuracyPercentage: Int
        get() = if (validQuestionCount == 0) {
            0
        } else {
            (validScore.toDouble() / validQuestionCount * 100).roundToInt()
        }
}
