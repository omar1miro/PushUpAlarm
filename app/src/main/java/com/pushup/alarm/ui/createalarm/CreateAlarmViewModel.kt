package com.pushup.alarm.ui.createalarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushup.alarm.alarm.AlarmScheduler
import com.pushup.alarm.data.model.Alarm
import com.pushup.alarm.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class CreateAlarmState(
    val hour: Int = 7,
    val minute: Int = 0,
    val pushUpCount: Int = 20,
    val label: String = "",
    val repeatDays: List<Int> = emptyList(),
    val isEditing: Boolean = false,
    val editingAlarmId: Long = -1L
)

@HiltViewModel
class CreateAlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: Long = savedStateHandle.get<Long>("alarmId") ?: -1L

    private val _state = MutableStateFlow(CreateAlarmState())
    val state: StateFlow<CreateAlarmState> = _state.asStateFlow()

    init {
        if (alarmId != -1L) {
            loadAlarm(alarmId)
        }
    }

    private fun loadAlarm(id: Long) {
        viewModelScope.launch {
            alarmRepository.getAlarmById(id)?.let { alarm ->
                val days = try {
                    Json.decodeFromString<List<Int>>(alarm.repeatDays)
                } catch (e: Exception) {
                    emptyList()
                }
                _state.value = CreateAlarmState(
                    hour = alarm.hour,
                    minute = alarm.minute,
                    pushUpCount = alarm.pushUpCount,
                    label = alarm.label,
                    repeatDays = days,
                    isEditing = true,
                    editingAlarmId = alarm.id
                )
            }
        }
    }

    fun updateHour(hour: Int) {
        _state.value = _state.value.copy(hour = hour)
    }

    fun updateMinute(minute: Int) {
        _state.value = _state.value.copy(minute = minute)
    }

    fun updatePushUpCount(count: Int) {
        _state.value = _state.value.copy(pushUpCount = count)
    }

    fun updateLabel(label: String) {
        _state.value = _state.value.copy(label = label)
    }

    fun toggleRepeatDay(day: Int) {
        val current = _state.value.repeatDays
        _state.value = _state.value.copy(
            repeatDays = if (day in current) current - day else current + day
        )
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            val repeatDaysJson = Json.encodeToString(s.repeatDays.sorted())

            val alarm = if (s.isEditing) {
                Alarm(
                    id = s.editingAlarmId,
                    hour = s.hour,
                    minute = s.minute,
                    pushUpCount = s.pushUpCount,
                    label = s.label,
                    repeatDays = repeatDaysJson,
                    isEnabled = true
                )
            } else {
                Alarm(
                    hour = s.hour,
                    minute = s.minute,
                    pushUpCount = s.pushUpCount,
                    label = s.label,
                    repeatDays = repeatDaysJson,
                    isEnabled = true
                )
            }

            val id = if (s.isEditing) {
                alarmRepository.updateAlarm(alarm)
                alarm.id
            } else {
                alarmRepository.insertAlarm(alarm)
            }

            alarmScheduler.schedule(alarm.copy(id = id))
            onSuccess()
        }
    }
}
