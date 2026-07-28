package com.example.wordquest.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: UserStatEntity)

    @Query("SELECT * FROM user_stats ORDER BY id DESC")
    fun getAllStats(): Flow<List<UserStatEntity>>

    @Query("DELETE FROM user_stats")
    suspend fun deleteAllStats()
}
