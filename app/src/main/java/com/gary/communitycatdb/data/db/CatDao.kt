package com.gary.communitycatdb.data.db

import androidx.room.*
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation
import com.gary.communitycatdb.data.model.CatWithLocations
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cats")
    fun getAllCats(): Flow<List<Cat>>

    @Query("SELECT * FROM cats WHERE name = :name")
    suspend fun getCatByName(name: String): Cat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: Cat)

    @Query("SELECT * FROM cat_locations WHERE catName = :catName")
    suspend fun getLocationsByCat(catName: String): List<CatLocation>

    @Insert
    suspend fun insertLocation(location: CatLocation)

    @Query("SELECT DISTINCT value FROM (SELECT favoriteFoods as value FROM cats)")
    suspend fun getAllFoodsRaw(): List<String>   // flatten 後用

    @Query("SELECT name FROM cats WHERE favoriteFoods LIKE '%' || :food || '%'")
    suspend fun getCatsByFavoriteFood(food: String): List<String>

    @Query("DELETE FROM cat_locations WHERE catName = :catName")
    suspend fun deleteLocationsByCatName(catName: String)

    @Transaction
    @Query("SELECT * FROM cats")
    fun getAllCatsWithLocations(): kotlinx.coroutines.flow.Flow<List<CatWithLocations>>

    @Query("DELETE FROM cat_locations WHERE id = :locationId")
    suspend fun deleteLocationById(locationId: Long)

}