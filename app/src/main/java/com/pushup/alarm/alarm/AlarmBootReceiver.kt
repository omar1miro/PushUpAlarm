package com.pushup.alarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pushup.alarm.data.db.AlarmDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var alarmDao: AlarmDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val enabledAlarms = alarmDao.getAllEnabled()
                    enabledAlarms.forEach { alarm ->
                        alarmScheduler.schedule(alarm)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
