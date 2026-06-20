package com.pushup.alarm.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pushup.alarm.MainActivity
import com.pushup.alarm.PushUpAlarmApp
import com.pushup.alarm.R
import com.pushup.alarm.challenge.ChallengeActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ALARM_FIRE) return

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val hour = intent.getIntExtra(EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_ALARM_MINUTE, 0)
        val pushUpCount = intent.getIntExtra(EXTRA_PUSH_UP_COUNT, 20)
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: ""

        showNotification(context, alarmId, hour, minute, pushUpCount, label)
        launchChallenge(context, alarmId, pushUpCount, label)
    }

    private fun launchChallenge(
        context: Context,
        alarmId: Long,
        pushUpCount: Int,
        label: String
    ) {
        val challengeIntent = Intent(context, ChallengeActivity::class.java).apply {
            putExtra(ChallengeActivity.EXTRA_ALARM_ID, alarmId)
            putExtra(ChallengeActivity.EXTRA_PUSH_UP_COUNT, pushUpCount)
            putExtra(ChallengeActivity.EXTRA_LABEL, label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        }
        context.startActivity(challengeIntent)
    }

    private fun showNotification(
        context: Context,
        alarmId: Long,
        hour: Int,
        minute: Int,
        pushUpCount: Int,
        label: String
    ) {
        val contentIntent = Intent(context, ChallengeActivity::class.java).apply {
            putExtra(ChallengeActivity.EXTRA_ALARM_ID, alarmId)
            putExtra(ChallengeActivity.EXTRA_PUSH_UP_COUNT, pushUpCount)
            putExtra(ChallengeActivity.EXTRA_LABEL, label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (label.isNotBlank()) label else "Wake Up!"
        val timeStr = String.format("%02d:%02d", hour, minute)
        val body = "Time: $timeStr — Complete $pushUpCount push-ups to dismiss"

        val notification = NotificationCompat.Builder(context, PushUpAlarmApp.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alarmId.toInt(), notification)
    }

    companion object {
        const val ACTION_ALARM_FIRE = "com.pushup.alarm.ALARM_FIRE"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_HOUR = "alarm_hour"
        const val EXTRA_ALARM_MINUTE = "alarm_minute"
        const val EXTRA_PUSH_UP_COUNT = "push_up_count"
        const val EXTRA_ALARM_LABEL = "alarm_label"
    }
}
