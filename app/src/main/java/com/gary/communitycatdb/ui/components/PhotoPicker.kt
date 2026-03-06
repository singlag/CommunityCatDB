package com.gary.communitycatdb.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gary.communitycatdb.util.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.gary.communitycatdb.util.FileUtil.savePhotoToInternalStorage

/**
 * 可重用的照片選擇器
 * @param photoPath 目前照片路徑（null = 無照片）
 * @param onPhotoSelected 選擇成功後回傳內部儲存的絕對路徑
 * @param modifier 
 */
@Composable
fun PhotoPicker(
    photoPath: String?,
    onPhotoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 照片選擇 Launcher
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = FileUtil.savePhotoToInternalStorage(context, it)
            //val savedPath = savePhotoToInternalStorage(context, it)
            onPhotoSelected(savedPath)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "貓隻照片",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { photoLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoPath != null) {
                AsyncImage(
                    model = photoPath,
                    contentDescription = "貓隻照片",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "點擊選擇照片\n（支援 JPG / PNG）",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (photoPath != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { photoLauncher.launch("image/*") }
            ) {
                Text("更換照片")
            }
        }
    }
}

