package com.gary.communitycatdb.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
}