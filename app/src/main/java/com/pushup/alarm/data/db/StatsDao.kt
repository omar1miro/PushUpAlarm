package com.pushup.alarm.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushup.alarm.data.model.DailyStats
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getByDate(date: String): DailyStats?

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyStats?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :days")
    fun getRecent(days: Int): Flow<List<DailyStats>>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAll(): Flow<List<DailyStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: DailyStats)

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT date FROM daily_stats
            WHERE streakDay = 1
            ORDER BY date DESC
        )
    """)
    suspend fun getMaxStreak(): Int
}
