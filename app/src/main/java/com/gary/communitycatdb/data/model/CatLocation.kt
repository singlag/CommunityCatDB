package com.gary.communitycatdb.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "cat_locations",
    foreignKeys = [ForeignKey(
        entity = Cat::class,
        parentColumns = ["name"],
        childColumns = ["catName"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CatLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catName: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)