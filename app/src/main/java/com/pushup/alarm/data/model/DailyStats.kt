package com.pushup.alarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStats(
    @PrimaryKey
    val date: String,
    val alarmsCompleted: Int = 0,
    val totalPushUps: Int = 0,
    val streakDay: Boolean = false
)
