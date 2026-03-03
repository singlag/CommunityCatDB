package com.gary.communitycatdb.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cats")
data class Cat(
    @PrimaryKey val name: String,           // 唯一主鍵
    val birthDate: LocalDate? = null,
    val gender: String = "未知",            // 公 / 母
    val isNeutered: Boolean = false,
    val father: String? = null,
    val mother: String? = null,
    val offspring: List<String> = emptyList(),
    val favoriteFoods: List<String> = emptyList(),
    val canTouch: Boolean = false,
    val canHandFeed: Boolean = false,
    val isMean: Boolean = false,
    val isShy: Boolean = false,
    val photoPath: String? = null,          // 內部儲存路徑
    val lastUpdated: Long = System.currentTimeMillis()
)