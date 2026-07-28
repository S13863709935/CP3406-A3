package com.example.wordquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val score: Int,
    val totalQuestions: Int
)
