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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CatViewModel @Inject constructor(
    private val repository: CatRepository   // ← 保持 private
) : ViewModel() {
	
	// 新增這行，讓 SettingScreen 可以用
    val repositoryPublic: CatRepository get() = repository   // 或者直接改名
	
	// 新增這兩個方法（之前我們提過）
    suspend fun getLocations(catName: String): List<CatLocation> =
        repository.getLocations(catName)

    fun clearSelectedCat() {
        _selectedCat.value = null
        _locations.value = emptyList()
    }
	
	
	val repository: CatRepository // 因為 SettingScreen 需要，直接暴露
	
    val cats: StateFlow<List<Cat>> = repository.allCats.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
                val points = locs.map { LatLng(it.latitude, it.longitude) }
                val polygon = map.addPolygon(
                    PolygonOptions()
                        .addAll(points)
                        .fillColor(0x44FF8800)   // 半透明橙色 mask
                        .strokeColor(Color.Orange.toArgb())
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

    // Export / Import
    fun exportDatabase(context: Context) {
        viewModelScope.launch {
            val allData = cats.value.map { cat ->
                val locs = repository.getLocations(cat.name)
                ExportData(cat, locs)
            }
            val json = Json.encodeToString(allData)
            val file = File(context.getExternalFilesDir(null), "community_cats_backup.json")
            file.writeText(json)
            // 分享 Intent (你可以在 UI 加按鈕呼叫)
        }
    }

    fun importDatabase(json: String) {
        viewModelScope.launch {
            val data: List<ExportData> = Json.decodeFromString(json)
            data.forEach { item ->
                repository.saveCat(item.cat)
                item.locations.forEach { repository.addLocation(it) }
            }
        }
    }	
}



@Serializable
data class ExportData(val cat: Cat, val locations: List<CatLocation>)