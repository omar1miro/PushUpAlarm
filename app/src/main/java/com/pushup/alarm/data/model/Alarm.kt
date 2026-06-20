package com.pushup.alarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val pushUpCount: Int,
    val label: String = "",
    val repeatDays: String = "[]",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
