package com.pushup.alarm.di

import android.content.Context
import androidx.room.Room
import com.pushup.alarm.data.db.AlarmDao
import com.pushup.alarm.data.db.AppDatabase
import com.pushup.alarm.data.db.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    @Singleton
    fun provideStatsDao(database: AppDatabase): StatsDao {
        return database.statsDao()
    }
}
