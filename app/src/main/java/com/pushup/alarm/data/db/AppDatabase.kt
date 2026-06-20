package com.pushup.alarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pushup.alarm.data.model.Alarm
import com.pushup.alarm.data.model.DailyStats

@Database(
    entities = [Alarm::class, DailyStats::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun statsDao(): StatsDao

    companion object {
        const val DATABASE_NAME = "pushup_alarm_db"
    }
}
