package com.example.wordquest.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizModeTest {

    @Test
    fun `flashcard value is restored from storage`() {
        assertEquals(
            QuizMode.FLASHCARD,
            QuizMode.fromStoredValue("FLASHCARD")
        )
    }

    @Test
    fun `multiple choice value is restored from storage`() {
        assertEquals(
            QuizMode.MULTIPLE_CHOICE,
            QuizMode.fromStoredValue("MULTIPLE_CHOICE")
        )
    }

    @Test
    fun `missing or invalid value falls back to flashcards`() {
        assertEquals(QuizMode.FLASHCARD, QuizMode.fromStoredValue(null))
        assertEquals(QuizMode.FLASHCARD, QuizMode.fromStoredValue(""))
        assertEquals(QuizMode.FLASHCARD, QuizMode.fromStoredValue("UNKNOWN"))
    }
}
