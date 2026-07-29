package com.example.wordquest.data.settings

enum class QuizMode {
    FLASHCARD,
    MULTIPLE_CHOICE;

    companion object {
        fun fromStoredValue(value: String?): QuizMode {
            return entries.firstOrNull { it.name == value } ?: FLASHCARD
        }
    }
}
