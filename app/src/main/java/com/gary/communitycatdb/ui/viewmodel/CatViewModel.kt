package com.gary.communitycatdb.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation
import com.gary.communitycatdb.data.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.gary.communitycatdb.data.model.CatWithLocations
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map


@HiltViewModel
class CatViewModel @Inject constructor(
    val repository: CatRepository   // ← 因為 SettingScreen 需要，直接暴露
) : ViewModel() {
	

    suspend fun getLocations(catName: String): List<CatLocation> =
        repository.getLocations(catName)

    fun clearSelectedCat() {
        _selectedCat.value = null
        _locations.value = emptyList()
    }


    //val cats: StateFlow<List<Cat>> = repository.allCats.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    // 替換原本的 val cats
    val catsWithLocations: StateFlow<List<CatWithLocations>> =
        repository.allCatsWithLocations.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    // 保留原有的 cats：透過 map 從完整資訊中提取出 Cat 列表
    // 這樣不需要額外去觀察資料庫，且能保證與 catsWithLocations 同步
    val cats: StateFlow<List<Cat>> = catsWithLocations
        .map { list -> list.map { it.cat } }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    private val _selectedCat = MutableStateFlow<Cat?>(null)
    val selectedCat: StateFlow<Cat?> = _selectedCat.asStateFlow()

    private val _locations = MutableStateFlow<List<CatLocation>>(emptyList())
    val locations: StateFlow<List<CatLocation>> = _locations.asStateFlow()

    // Global Search Result
    private val _searchResults = MutableStateFlow<List<Any>>(emptyList())
    val searchResults: StateFlow<List<Any>> = _searchResults.asStateFlow()

    fun loadCatAndLocations(catName: String) {
        viewModelScope.launch {
            val cat = repository.getCat(catName) ?: return@launch
            _selectedCat.value = cat
            _locations.value = repository.getLocations(catName)
        }
    }

    fun onMarkerClick(catName: String, map: GoogleMap) {
        loadCatAndLocations(catName)
        viewModelScope.launch {
            val locs = repository.getLocations(catName)
            if (locs.size >= 3) {
                //val points = locs.map { LatLng(it.latitude, it.longitude) }
                // 改指定 it 為 CatLocation 類型
                val points = locs.map { loc: CatLocation -> LatLng(loc.latitude, loc.longitude) }
                val polygon = map.addPolygon(
                    PolygonOptions()
                        .addAll(points)
                        .fillColor(0x44FF8800)   // 半透明橙色 mask
                        //.strokeColor(Color.Orange.toArgb())
                        // 使用手動定義的橙色 (ARGB)
                        .strokeColor(Color(0xFFFF8800).toArgb())
                        .strokeWidth(6f)
                )
                delay(6000L)
                polygon.remove()
            }
        }
    }

    fun saveCat(cat: Cat) {
        viewModelScope.launch { repository.saveCat(cat) }
    }

    fun addLocation(catName: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.addLocation(CatLocation(catName = catName, latitude = lat, longitude = lng))
        }
    }

    // 新增：刪除特定位置的函數
    fun deleteLocationById(locationId: Long) {
        viewModelScope.launch {
            repository.deleteLocationById(locationId)
        }
    }

    // Global Search
    fun performSearch(query: String) {
        viewModelScope.launch {
            val q = query.trim().lowercase()
            if (q.isEmpty()) {
                _searchResults.value = emptyList()
                return@launch
            }
            val cat = repository.getCat(query)
            if (cat != null) {
                _searchResults.value = listOf(cat)
                return@launch
            }
            val catsByFood = repository.getCatsByFood(q)
            _searchResults.value = catsByFood.map { it }  // 食物結果顯示貓名列表
        }
    }


}



@Serializable
data class ExportData(val cat: Cat, val locations: List<CatLocation>)