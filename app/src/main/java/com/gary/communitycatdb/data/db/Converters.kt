package com.gary.communitycatdb.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date // 確保是這一個
import kotlinx.datetime.LocalDate // 加入這一行



class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDays()?.toLong()

    @TypeConverter
    fun toLocalDate(millis: Long?): LocalDate? = millis?.let { LocalDate.fromEpochDays(it.toInt()) }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

}