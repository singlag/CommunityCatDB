package com.gary.communitycatdb.di

import android.content.Context
import androidx.room.Room
import com.gary.communitycatdb.data.db.CatDatabase
import com.gary.communitycatdb.data.db.CatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CatDatabase {
        // 告訴 Hilt 如何建立 CatDatabase 實例
        return Room.databaseBuilder(
            context,
            CatDatabase::class.java,
            "community_cat_db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true) // 建議開發初期加入，避免修改欄位後報錯

            .build()
    }

    @Provides
    fun provideCatDao(database: CatDatabase): CatDao {
        // 告訴 Hilt 如何從 database 取得 CatDao
        return database.catDao()
    }
}
