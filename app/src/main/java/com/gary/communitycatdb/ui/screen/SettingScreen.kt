package com.gary.communitycatdb.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.hilt.navigation.compose.hiltViewModel
import com.gary.communitycatdb.data.model.CatLocation
import com.gary.communitycatdb.ui.viewmodel.CatViewModel
import com.gary.communitycatdb.util.ExportImportUtil
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import kotlinx.coroutines.launch
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    viewModel: CatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cats by viewModel.cats.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val count = ExportImportUtil.importDatabase(context, it, viewModel.repository) // repository 已注入 ViewModel
                if (count > 0) {
                    Toast.makeText(context, "成功匯入 $count 隻貓！", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "匯入失敗，請檢查檔案", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "社區貓資料庫備份",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "目前共有 ${cats.size} 隻貓",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(32.dp))

            // Export 按鈕
            Button(
                onClick = {
                    coroutineScope.launch {
                        // 1. 產生備份檔案 (現在直接回傳 File? 而不是 Uri?)
                        val backupFile = ExportImportUtil.exportDatabase(context, cats, viewModel.repository)

                        if (backupFile != null && backupFile.exists()) {
                            // 2. 直接使用回傳的 File 物件進行移動，不再需要 listFiles() 去搜尋
                            val savedFile = ExportImportUtil.moveFileToDownloads(context, backupFile)

                            if (savedFile != null) {
                                Toast.makeText(
                                    context,
                                    "備份已存至 Downloads\n檔名：${savedFile.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(context, "移動檔案至 Downloads 失敗", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "產生 ZIP 備份檔失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("匯出資料庫（ZIP）")
            }

            // Import 按鈕
            Button(
                onClick = { importLauncher.launch("application/zip") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("匯入資料庫（選擇 ZIP 檔案）")
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "備份會包含所有貓隻資料、多個位置、照片路徑等\n匯入時會自動覆蓋同名貓隻\n＊＊＊　為保障社區貓安全，請勿公開分享社區貓資料　＊＊＊",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}