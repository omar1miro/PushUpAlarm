package com.pushup.alarm.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushup.alarm.alarm.AlarmScheduler
import com.pushup.alarm.data.model.Alarm
import com.pushup.alarm.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val alarms = alarmRepository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmRepository.updateAlarm(updated)
            if (updated.isEnabled) {
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(updated)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmScheduler.cancel(alarm)
            alarmRepository.deleteAlarm(alarm)
        }
    }
}
