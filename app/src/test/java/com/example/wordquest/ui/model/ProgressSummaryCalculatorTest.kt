package com.example.wordquest.ui.model

import com.example.wordquest.data.local.UserStatEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressSummaryCalculatorTest {

    @Test
    fun `empty history produces an empty summary`() {
        val summary = ProgressSummaryCalculator.calculate(
            stats = emptyList(),
            today = "2026-08-01"
        )

        assertEquals(ProgressSummary(), summary)
    }

    @Test
    fun `summary calculates weighted accuracy and chronological trend`() {
        val statsNewestFirst = listOf(
            UserStatEntity(
                id = 3,
                date = "2026-08-01 12:00",
                score = 9,
                totalQuestions = 10
            ),
            UserStatEntity(
                id = 2,
                date = "2026-08-01 09:00",
                score = 1,
                totalQuestions = 5
            ),
            UserStatEntity(
                id = 1,
                date = "2026-07-31 18:00",
                score = 2,
                totalQuestions = 4
            )
        )

        val summary = ProgressSummaryCalculator.calculate(
            stats = statsNewestFirst,
            today = "2026-08-01"
        )

        assertEquals(3, summary.totalQuizzes)
        assertEquals(19, summary.totalQuestions)
        assertEquals(12, summary.correctAnswers)
        assertEquals(63, summary.averageAccuracy)
        assertEquals(90, summary.bestAccuracy)
        assertEquals(15, summary.practicedToday)
        assertEquals(listOf(50, 20, 90), summary.recentAccuracies)
    }

    @Test
    fun `invalid stored values are kept within valid accuracy bounds`() {
        val stats = listOf(
            UserStatEntity(
                id = 1,
                date = "2026-08-01 10:00",
                score = 8,
                totalQuestions = 0
            ),
            UserStatEntity(
                id = 2,
                date = "2026-08-01 11:00",
                score = 12,
                totalQuestions = 10
            )
        )

        val summary = ProgressSummaryCalculator.calculate(
            stats = stats,
            today = "2026-08-01"
        )

        assertEquals(10, summary.totalQuestions)
        assertEquals(10, summary.correctAnswers)
        assertEquals(100, summary.averageAccuracy)
        assertEquals(100, summary.bestAccuracy)
        assertEquals(listOf(100, 0), summary.recentAccuracies)
    }
}
