package com.pushup.alarm.data.repository

import com.pushup.alarm.data.db.StatsDao
import com.pushup.alarm.data.model.DailyStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val statsDao: StatsDao
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTodayStats(): Flow<DailyStats?> {
        return statsDao.observeByDate(LocalDate.now().format(dateFormatter))
    }

    fun getRecentStats(days: Int): Flow<List<DailyStats>> {
        return statsDao.getRecent(days)
    }

    fun getAllStats(): Flow<List<DailyStats>> = statsDao.getAll()

    suspend fun recordAlarmCompleted(pushUpCount: Int) {
        val today = LocalDate.now().format(dateFormatter)
        val existing = statsDao.getByDate(today)

        val updated = if (existing != null) {
            existing.copy(
                alarmsCompleted = existing.alarmsCompleted + 1,
                totalPushUps = existing.totalPushUps + pushUpCount,
                streakDay = true
            )
        } else {
            DailyStats(
                date = today,
                alarmsCompleted = 1,
                totalPushUps = pushUpCount,
                streakDay = true
            )
        }
        statsDao.upsert(updated)
    }

    suspend fun markStreakDay(completed: Boolean) {
        val today = LocalDate.now().format(dateFormatter)
        val existing = statsDao.getByDate(today)

        val updated = if (existing != null) {
            existing.copy(streakDay = completed)
        } else {
            DailyStats(
                date = today,
                alarmsCompleted = 0,
                totalPushUps = 0,
                streakDay = completed
            )
        }
        statsDao.upsert(updated)
    }

    suspend fun getMaxStreak(): Int = statsDao.getMaxStreak()
}
