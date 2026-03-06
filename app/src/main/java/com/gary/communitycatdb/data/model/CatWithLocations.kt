package com.gary.communitycatdb.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CatWithLocations(
    @Embedded val cat: Cat,
    @Relation(
        parentColumn = "name",    // Cat 的主鍵
        entityColumn = "catName"  // CatLocation 中對應的欄位
    )
    val locations: List<CatLocation>
)
