package com.pushup.alarm.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class Converters {

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
