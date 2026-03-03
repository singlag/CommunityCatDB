package com.gary.communitycatdb.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation

@Database(entities = [Cat::class, CatLocation::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CatDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
}