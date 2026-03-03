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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gary.communitycatdb.ui.viewmodel.CatViewModel
import com.gary.communitycatdb.util.ExportImportUtil
import kotlinx.coroutines.launch

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
                        val uri = ExportImportUtil.exportDatabase(context, cats, viewModel.repository)
                        uri?.let {
                            val intent = ExportImportUtil.createShareIntent(context, it)
                            context.startActivity(Intent.createChooser(intent, "分享備份檔案"))
                        } ?: run {
                            Toast.makeText(context, "匯出失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("匯出資料庫（JSON）")
            }

            // Import 按鈕
            Button(
                onClick = { importLauncher.launch("application/json") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("匯入資料庫（選擇 JSON 檔案）")
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "備份會包含所有貓隻資料、多個位置、照片路徑等\n匯入時會自動覆蓋同名貓隻",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}