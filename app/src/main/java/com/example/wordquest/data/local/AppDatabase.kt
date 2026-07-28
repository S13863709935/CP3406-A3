package com.example.wordquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserStatEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
