package com.gary.communitycatdb.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDetailSheet(
    cat: Cat,
    locations: List<CatLocation>,
    currentLocation: CatLocation?, // 新增：目前點擊的那個特定位置
    onEdit: () -> Unit,
    onDeleteLocation: (Long) -> Unit // 新增：刪除回呼
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (cat.photoPath != null) {
            AsyncImage(
                model = cat.photoPath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(cat.name, style = MaterialTheme.typography.headlineMedium)

        DetailRow("出生日期", cat.birthDate?.toString() ?: "未知")
        DetailRow("性別", cat.gender)
        DetailRow("已絕育", if (cat.isNeutered) "是" else "否")
        DetailRow("父親", cat.father ?: "未知")
        DetailRow("母親", cat.mother ?: "未知")
        DetailRow("子女", cat.offspring.joinToString())
        DetailRow("喜愛食物", cat.favoriteFoods.joinToString())
        DetailRow("可摸", if (cat.canTouch) "是" else "否")
        DetailRow("可手餵", if (cat.canHandFeed) "是" else "否")
        DetailRow("惡貓", if (cat.isMean) "是" else "否")
        DetailRow("怕人", if (cat.isShy) "是" else "否")
        DetailRow("出現位置", "${locations.size} 個")

        Spacer(Modifier.height(16.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
            Text("編輯貓隻資料")
        }


        // --- 新增：移除此位置按鈕 ---
        if (currentLocation != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onDeleteLocation(currentLocation.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("移除此地圖標記 (Marker)")
            }
        }

    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label：", style = MaterialTheme.typography.labelLarge)
        Text(value)
    }
}