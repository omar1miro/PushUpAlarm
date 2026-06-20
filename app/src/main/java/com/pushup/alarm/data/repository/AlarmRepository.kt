package com.pushup.alarm.data.repository

import com.pushup.alarm.data.db.AlarmDao
import com.pushup.alarm.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {

    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAll()

    fun getEnabledAlarms(): Flow<List<Alarm>> = alarmDao.getEnabled()

    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getById(id)

    suspend fun getAllEnabledAlarms(): List<Alarm> = alarmDao.getAllEnabled()

    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insert(alarm)

    suspend fun updateAlarm(alarm: Alarm) = alarmDao.update(alarm)

    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.delete(alarm)

    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteById(id)
}
