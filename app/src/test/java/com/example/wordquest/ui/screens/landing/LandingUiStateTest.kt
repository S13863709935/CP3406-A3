package com.example.wordquest.ui.screens.landing

import com.example.wordquest.data.local.UserStatEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LandingUiStateTest {

    @Test
    fun `dashboard keeps the five newest sessions and shared summary`() {
        val statsNewestFirst = (6 downTo 1).map { id ->
            UserStatEntity(
                id = id,
                date = "2026-08-03 10:0$id",
                score = id,
                totalQuestions = 10
            )
        }

        val state = LandingUiState.from(
            stats = statsNewestFirst,
            dailyGoal = 20,
            today = "2026-08-03"
        )

        assertEquals(20, state.dailyGoal)
        assertEquals(listOf(6, 5, 4, 3, 2), state.recentStats.map { it.id })
        assertEquals(6, state.summary.totalQuizzes)
        assertEquals(60, state.summary.practicedToday)
        assertEquals(35, state.summary.averageAccuracy)
    }
}
