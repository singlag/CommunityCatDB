package com.gary.communitycatdb.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gary.communitycatdb.data.model.CatLocation
import com.gary.communitycatdb.ui.components.CatEditDialog
import com.gary.communitycatdb.ui.viewmodel.CatViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.GoogleMap


@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(viewModel: CatViewModel = hiltViewModel()) {
    val cats by viewModel.cats.collectAsState()
    val selectedCat by viewModel.selectedCat.collectAsState()
    val locations by viewModel.locations.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showSettingScreen by remember { mutableStateOf(false) }

    //var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var googleMap: GoogleMap? by remember { mutableStateOf<GoogleMap?>(null) }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社區貓資料庫 🐱") },
                actions = {
                    // Search Box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.performSearch(it)
                        },
                        placeholder = { Text("搜尋貓名或食物") },
                        modifier = Modifier.width(240.dp)
                    )
                    IconButton(onClick = { showSettingScreen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(padding),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(22.3964, 114.1095), 11f) // 香港中心
                },
                //onMapLoaded = { googleMap = it },
                onMapLoaded = { /* 這裡現在不提供參數了 */ },
                onMapLongClick = { latLng ->
                    editingLatLng = latLng
                    showEditDialog = true
                }
            ) {

                // 透過 MapEffect 獲取 GoogleMap 實例
                MapEffect(Unit) { map ->
                    googleMap = map
                }

                // 畫所有貓的位置 marker
                cats.forEach { cat ->
                    val catLocs = remember(cat.name) { mutableStateOf<List<CatLocation>>(emptyList()) }
                    LaunchedEffect(cat.name) {
                        catLocs.value = viewModel.getLocations(cat.name)
                    }
                    catLocs.value.forEach { loc ->
                        Marker(
                            state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                            title = cat.name,
                            snippet = "點擊查看資料",
                            onClick = {
                                viewModel.onMarkerClick(cat.name, googleMap!!)
                                true
                            }
                        )
                    }
                }
            }

            // Cat 詳細 Bottom Sheet
            if (selectedCat != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.clearSelectedCat() }
                ) {
                    CatDetailSheet(
                        cat = selectedCat!!,
                        locations = locations,
                        onEdit = {
                            // 編輯時可傳入目前位置（簡化為 null）
                            showEditDialog = true
                        }
                    )
                }
            }
        }
    }

    // 新增/編輯 Dialog
    if (showEditDialog) {
        CatEditDialog(
            initialLatLng = editingLatLng,
            onDismiss = {
                showEditDialog = false
                editingLatLng = null
            },
            onSave = { /* 可選額外處理 */ }
        )
    }

    // Setting Screen（全屏替換）
    if (showSettingScreen) {
        SettingScreen(
            onBack = { showSettingScreen = false }
        )
    }
}