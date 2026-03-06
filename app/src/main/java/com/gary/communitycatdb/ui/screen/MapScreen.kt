package com.gary.communitycatdb.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation
import com.gary.communitycatdb.ui.components.CatEditDialog
import com.gary.communitycatdb.ui.viewmodel.CatViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.*



@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(viewModel: CatViewModel = hiltViewModel()) {
    //val cats by viewModel.cats.collectAsState()
    val catsWithLocs by viewModel.catsWithLocations.collectAsState()
    val selectedCat by viewModel.selectedCat.collectAsState()
    val locations by viewModel.locations.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showSettingScreen by remember { mutableStateOf(false) }

    // 預設為一般地圖
    var mapType by remember { mutableStateOf(MapType.HYBRID) }
    var showMapTypeMenu by remember { mutableStateOf(false) }

    // 定義 Scope 以便執行 launc
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 定義位置權限狀態 (精確 + 粗略位置)
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 建立位置服務客戶端
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 將 CameraPositionState 提取到 GoogleMap 外部
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(22.3964, 114.1095), 11f)
    }

    // 定義相片權限 (Android 13+ 使用 READ_MEDIA_IMAGES)
    val photoPermissionState = rememberPermissionState(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    var googleMap: GoogleMap? by remember { mutableStateOf<GoogleMap?>(null) }


    var showBottomSheet by remember { mutableStateOf(false) } // 控制 Sheet 是否顯示
    var currentCat  by remember { mutableStateOf<Cat?>(null) } // 存儲目前點擊的是哪隻貓
    var currentLocation  by remember { mutableStateOf<CatLocation?>(null) } // 存儲目前點擊的是哪個特定 Marker 位置
    val sheetState = rememberModalBottomSheetState() // 管理 Sheet 的展開/收起狀態


    // 修改原本的 LaunchedEffect(67-71行)
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        scope.launch {
                            // 自動移動鏡頭到當前位置，縮放層級設為 15f
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(it.latitude, it.longitude), 15f
                                )
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                // 處理安全性異常
            }
        } else {
            // 如果還沒拿權限，先請求
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }


    Scaffold(
        topBar = {
            // 使用 Column 包裹標題和操作列
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // 確保內容不會被手機頂部狀態欄遮住
                    .padding(bottom = 4.dp)
            ) {
                // 第一行：標題置中
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "社區貓資料庫 🐱",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // 第二行：搜尋框與功能按鈕
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Box (佔用剩餘空間)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.performSearch(it)
                        },
                        placeholder = { Text("搜尋貓名或食物") },
                        modifier = Modifier.weight(1f), // 使用 weight 自動填滿左側
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    // 功能按鈕
                    IconButton(onClick = { showSettingScreen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }

                    Box {
                        IconButton(onClick = { showMapTypeMenu = true }) {
                            // 如果沒匯入 extended 庫，請記得改回 Icons.Default.Map
                            Icon(Icons.Default.Layers, contentDescription = "切換圖層")
                        }

                        DropdownMenu(
                            expanded = showMapTypeMenu,
                            onDismissRequest = { showMapTypeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("一般地圖") },
                                onClick = { mapType = MapType.NORMAL; showMapTypeMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("衛星圖") },
                                onClick = { mapType = MapType.SATELLITE; showMapTypeMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("混合圖") },
                                onClick = { mapType = MapType.HYBRID; showMapTypeMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("地形圖") },
                                onClick = { mapType = MapType.TERRAIN; showMapTypeMenu = false }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 按下按鈕時，如果沒權限就再請求一次，有權限就執行定位
                    if (locationPermissionState.allPermissionsGranted) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                                        )
                                    }
                                }
                            }
                        } catch (e: SecurityException) { /* 處理異常 */ }
                    } else {
                        locationPermissionState.launchMultiplePermissionRequest()
                    }
                }
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "我的位置")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(padding),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionState.allPermissionsGranted, // 顯示藍點 (需檢查權限)
                    mapType = mapType
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false, // 隱藏原生按鈕，改用自訂
                    zoomControlsEnabled = false // 加入這一行，隱藏右下角的 +- 按鈕
                    ),
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

                /*
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
                */


                /*
                // 畫所有貓的位置 marker
                catsWithLocs.forEach { item ->
                    val cat = item.cat
                    val locs = item.locations // 直接拿取，不需再透過 LaunchedEffect 異步抓取

                    locs.forEach { loc ->
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
                 */


                // 畫所有貓的位置 marker
                catsWithLocs.forEach { item ->
                    item.locations.forEach { loc ->
                        // 使用 key 確保 Compose 能追蹤特定的位置點
                        key(loc.id) {
                            Marker(
                                state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                                title = item.cat.name,
                                snippet = "點擊查看資料",
                                onClick = {
                                    viewModel.onMarkerClick(item.cat.name, googleMap!!)

                                    currentCat  = item.cat
                                    currentLocation  = loc // 儲存點擊的這一個特定位置
                                    showBottomSheet = true


                                    true
                                }
                            )
                        }
                    }
                }

            }

            /*
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

             */

            if (showBottomSheet && currentCat != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    CatDetailSheet(
                        cat = currentCat!!,
                        // 獲取該貓隻所有的位置列表供顯示數量使用
                        locations = catsWithLocs.find { it.cat.name == selectedCat!!.name }?.locations
                            ?: emptyList(),
                        currentLocation = currentLocation, // 傳入當前點擊的特定位置
                        onEdit = {
                            showBottomSheet = false
                            showEditDialog = true // 開啟編輯 Dialog
                        },
                        onDeleteLocation = { locId ->
                            // 執行刪除特定位置
                            viewModel.deleteLocationById(locId)
                            showBottomSheet = false // 刪除後自動關閉
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