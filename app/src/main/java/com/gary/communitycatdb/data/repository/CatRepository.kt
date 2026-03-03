package com.gary.communitycatdb.data.repository

import com.gary.communitycatdb.data.db.CatDatabase
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatRepository @Inject constructor(private val db: CatDatabase) {
    val allCats: Flow<List<Cat>> = db.catDao().getAllCats()

    suspend fun getCat(name: String): Cat? = db.catDao().getCatByName(name)
    suspend fun saveCat(cat: Cat) = db.catDao().insertCat(cat)
    suspend fun getLocations(catName: String): List<CatLocation> = db.catDao().getLocationsByCat(catName)
    suspend fun addLocation(location: CatLocation) = db.catDao().insertLocation(location)
    suspend fun getAllFoods(): List<String> = db.catDao().getAllFoodsRaw().flatMap { it.split(",") }.distinct()
    suspend fun getCatsByFood(food: String): List<String> = db.catDao().getCatsByFavoriteFood(food)
}