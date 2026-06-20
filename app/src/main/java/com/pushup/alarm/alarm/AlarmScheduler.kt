package com.pushup.alarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pushup.alarm.data.model.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        val pendingIntent = createPendingIntent(alarm)
        val triggerTime = calculateNextTriggerTime(alarm)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancel(alarm: Alarm) {
        val pendingIntent = createPendingIntent(alarm)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createPendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(AlarmReceiver.EXTRA_PUSH_UP_COUNT, alarm.pushUpCount)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            action = AlarmReceiver.ACTION_ALARM_FIRE
        }

        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeatDays = try {
            kotlinx.serialization.json.Json.decodeFromString<List<Int>>(alarm.repeatDays)
        } catch (e: Exception) {
            emptyList()
        }

        if (repeatDays.isEmpty()) {
            if (trigger.timeInMillis <= now.timeInMillis) {
                trigger.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            val mappedCurrentDay = mapCalendarDayToAlarmDay(currentDayOfWeek)

            if (trigger.timeInMillis <= now.timeInMillis) {
                trigger.add(Calendar.DAY_OF_YEAR, 1)
            }

            val nextTriggerDayOfWeek = mapCalendarDayToAlarmDay(trigger.get(Calendar.DAY_OF_WEEK))
            if (nextTriggerDayOfWeek !in repeatDays) {
                var daysToAdd = 1
                while (daysToAdd <= 7) {
                    trigger.add(Calendar.DAY_OF_YEAR, 1)
                    val testDay = mapCalendarDayToAlarmDay(trigger.get(Calendar.DAY_OF_WEEK))
                    if (testDay in repeatDays) break
                    daysToAdd++
                }
            }
        }

        return trigger.timeInMillis
    }

    private fun mapCalendarDayToAlarmDay(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
