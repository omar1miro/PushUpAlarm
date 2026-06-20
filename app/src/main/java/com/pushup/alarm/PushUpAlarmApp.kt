package com.pushup.alarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PushUpAlarmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(alarmChannel)
    }

    companion object {
        const val CHANNEL_ALARM = "alarm_channel"
    }
}
