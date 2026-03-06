package com.gary.communitycatdb.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.ui.viewmodel.CatViewModel
import com.gary.communitycatdb.util.FileUtil
import com.gary.communitycatdb.util.FileUtil.savePhotoToInternalStorage
import com.google.android.gms.maps.model.LatLng
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatEditDialog(
    initialCat: Cat? = null,
    initialLatLng: LatLng? = null,   // 長按地圖傳入的新位置
    onDismiss: () -> Unit,
    onSave: (Cat) -> Unit,
    viewModel: CatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cats by viewModel.cats.collectAsState()   // 用來檢查貓名唯一 + autocomplete

    // 表單狀態
    var catName by remember { mutableStateOf(initialCat?.name ?: "") }
    var birthDate by remember { mutableStateOf<LocalDate?>(initialCat?.birthDate) }  // ← 加 <LocalDate?>
    var gender by remember { mutableStateOf(initialCat?.gender ?: "未知") }
    var isNeutered by remember { mutableStateOf(initialCat?.isNeutered ?: false) }
    var father by remember { mutableStateOf(initialCat?.father ?: "") }
    var mother by remember { mutableStateOf(initialCat?.mother ?: "") }
    //var offspring by remember { mutableStateOf(initialCat?.offspring?.toMutableList() ?: mutableListOf()) }
    //var offspring by remember { mutableStateListOf<String>(*initialCat?.offspring?.toTypedArray() ?: emptyArray()) }
    val offspring = remember { mutableStateListOf<String>().apply { initialCat?.offspring?.let { addAll(it) } } }
    //var favoriteFoods by remember { mutableStateOf(initialCat?.favoriteFoods?.toMutableList() ?: mutableListOf()) }
    val favoriteFoods = remember { mutableStateListOf<String>().apply { initialCat?.favoriteFoods?.let { addAll(it) } } }
    var canTouch by remember { mutableStateOf(initialCat?.canTouch ?: false) }
    var canHandFeed by remember { mutableStateOf(initialCat?.canHandFeed ?: false) }
    var isMean by remember { mutableStateOf(initialCat?.isMean ?: false) }
    var isShy by remember { mutableStateOf(initialCat?.isShy ?: false) }
    var photoPath by remember { mutableStateOf<String?>(initialCat?.photoPath) }  // ← 加 <String?>


    // Autocomplete 展開狀態
    var fatherExpanded by remember { mutableStateOf(false) }
    var motherExpanded by remember { mutableStateOf(false) }
    var foodExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = birthDate?.toEpochDays()?.times(86400000L)  // 轉成 millis
    )

    // 照片選擇器
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            //photoPath = savePhotoToInternalStorage(context, it)
            photoPath = FileUtil.savePhotoToInternalStorage(context, it)
        }
    }

    // 檢查貓名是否已存在（輸入時即時檢查）
    val existingCat = cats.find { it.name.equals(catName, ignoreCase = true) }
    val isNameUnique = initialCat != null || existingCat == null || existingCat.name.equals(catName, ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = if (initialCat == null) "新增社區貓" else "編輯 ${initialCat.name}",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(24.dp))

                // 貓名（唯一，必填）
                OutlinedTextField(
                    value = catName,
                    onValueChange = { catName = it },
                    label = { Text("貓名（唯一，必填）") },
                    isError = catName.isBlank() || !isNameUnique,
                    supportingText = {
                        if (catName.isNotBlank() && !isNameUnique) {
                            Text("此貓名已存在，將載入現有資料", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 如果輸入同名，自動 load 資料
                LaunchedEffect(catName) {
                    if (catName.length > 2 && existingCat != null && initialCat == null) {
                        // 自動載入
                        birthDate = existingCat.birthDate
                        gender = existingCat.gender
                        isNeutered = existingCat.isNeutered
                        father = existingCat.father ?: ""
                        mother = existingCat.mother ?: ""
                        //offspring = existingCat.offspring.toMutableList()
                        //favoriteFoods = existingCat.favoriteFoods.toMutableList()
                        offspring.clear()
                        offspring.addAll(existingCat.offspring)
                        favoriteFoods.clear()
                        favoriteFoods.addAll(existingCat.favoriteFoods)
                        canTouch = existingCat.canTouch
                        canHandFeed = existingCat.canHandFeed
                        isMean = existingCat.isMean
                        isShy = existingCat.isShy
                        photoPath = existingCat.photoPath
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 出生日期
                OutlinedTextField(
                    value = birthDate?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("出生日期") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            //Icon(Icons.Default.Add, contentDescription = "選日期")
                            Icon(imageVector = Icons.Default.Add, contentDescription = "選日期") // 確保參數名正確
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 性別
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = gender == "公",
                        onClick = { gender = "公" },
                        label = { Text("公") }
                    )
                    FilterChip(
                        selected = gender == "母",
                        onClick = { gender = "母" },
                        label = { Text("母") }
                    )
                    FilterChip(
                        selected = gender == "未知",
                        onClick = { gender = "未知" },
                        label = { Text("未知") }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 已絕育
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isNeutered, onCheckedChange = { isNeutered = it })
                    Spacer(Modifier.width(8.dp))
                    Text("已絕育")
                }

                Spacer(Modifier.height(16.dp))

                // 父親 Autocomplete
                ExposedDropdownMenuBox(
                    expanded = fatherExpanded,
                    onExpandedChange = { fatherExpanded = it }
                ) {
                    OutlinedTextField(
                        value = father,
                        onValueChange = { father = it },
                        label = { Text("父親") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fatherExpanded) },
                        modifier = Modifier
                            //.menuAnchor()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fatherExpanded,
                        onDismissRequest = { fatherExpanded = false }
                    ) {
                        cats.filter { it.name != catName }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    father = cat.name
                                    fatherExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 母親 Autocomplete
                ExposedDropdownMenuBox(
                    expanded = motherExpanded,
                    onExpandedChange = { motherExpanded = it }
                ) {
                    OutlinedTextField(
                        value = mother,
                        onValueChange = { mother = it },
                        label = { Text("母親") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = motherExpanded) },
                        modifier = Modifier
                            //.menuAnchor()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = motherExpanded,
                        onDismissRequest = { motherExpanded = false }
                    ) {
                        cats.filter { it.name != catName }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    mother = cat.name
                                    motherExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 子女（多選）
                Text("子女", style = MaterialTheme.typography.titleMedium)
                Row {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("新增子女") },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (offspring.isNotEmpty() && offspring.last().isNotBlank()) {
                                    // 已用 chips 處理
                                }
                            }) {
                                Icon(Icons.Default.Add, null)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    offspring.forEachIndexed { index, child ->
                        AssistChip(
                            onClick = { offspring.removeAt(index) },
                            label = { Text(child) },
                            trailingIcon = { Icon(Icons.Default.Close, null) }
                        )
                    }
                }
                // 簡化版：點擊後彈 Dialog 選（實際生產可再優化）
                Button(onClick = {
                    // 這裡可再開一個子 Dialog 選子女，這裡用 Toast 示範
                    Toast.makeText(context, "子女多選請從下拉選擇（完整版已支援）", Toast.LENGTH_SHORT).show()
                }) {
                    Text("新增子女")
                }

                Spacer(Modifier.height(16.dp))

                // 喜愛食物（多選 Autocomplete）
                Text("喜愛食物", style = MaterialTheme.typography.titleMedium)
                ExposedDropdownMenuBox(
                    expanded = foodExpanded,
                    onExpandedChange = { foodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("新增食物") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = foodExpanded) },
                        //modifier = Modifier.menuAnchor().fillMaxWidth()
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = foodExpanded,
                        onDismissRequest = { foodExpanded = false }
                    ) {
                        // 從所有貓的食物 flatten 取得建議
                        val allFoods = cats.flatMap { it.favoriteFoods }.distinct()
                        allFoods.forEach { food ->
                            DropdownMenuItem(
                                text = { Text(food) },
                                onClick = {
                                    if (!favoriteFoods.contains(food)) favoriteFoods.add(food)
                                    foodExpanded = false
                                }
                            )
                        }
                    }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favoriteFoods.forEachIndexed { index, food ->
                        AssistChip(
                            onClick = { favoriteFoods.removeAt(index) },
                            label = { Text(food) },
                            trailingIcon = { Icon(Icons.Default.Close, null) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 可摸、可手餵、惡貓、怕人
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = canTouch, onCheckedChange = { canTouch = it })
                        Spacer(Modifier.width(12.dp))
                        Text("可摸")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = canHandFeed, onCheckedChange = { canHandFeed = it })
                        Spacer(Modifier.width(12.dp))
                        Text("可手餵")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isMean, onCheckedChange = { isMean = it })
                        Spacer(Modifier.width(12.dp))
                        Text("惡貓")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isShy, onCheckedChange = { isShy = it })
                        Spacer(Modifier.width(12.dp))
                        Text("怕人")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 照片上傳
                Text("照片", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoPath != null) {
                        AsyncImage(
                            model = photoPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("未選擇照片")
                    }
                }
                Button(
                    onClick = { photoLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("選擇 / 更換照片")
                }

                Spacer(Modifier.height(32.dp))

                // 儲存按鈕
                Button(
                    onClick = {
                        if (catName.isBlank()) {
                            Toast.makeText(context, "貓名不能空白！", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newCat = Cat(
                            name = catName.trim(),
                            birthDate = birthDate,
                            gender = gender,
                            isNeutered = isNeutered,
                            father = father.ifBlank { null },
                            mother = mother.ifBlank { null },
                            offspring = offspring,
                            favoriteFoods = favoriteFoods,
                            canTouch = canTouch,
                            canHandFeed = canHandFeed,
                            isMean = isMean,
                            isShy = isShy,
                            photoPath = photoPath,
                            lastUpdated = System.currentTimeMillis()
                        )

                        viewModel.saveCat(newCat)

                        // 如果有新位置，就新增位置
                        initialLatLng?.let {
                            viewModel.addLocation(catName.trim(), it.latitude, it.longitude)
                        }

                        Toast.makeText(context, "儲存成功！", Toast.LENGTH_SHORT).show()
                        onSave(newCat)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("儲存貓隻資料")
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }

    // 日期選擇器
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 這裡取得選擇的日期並儲存
                        val selectedMillis = datePickerState.selectedDateMillis
                        birthDate = selectedMillis?.let {
                            java.time.Instant.ofEpochMilli(it)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toKotlinLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)  // 記得先定義 datePickerState
        }
    }
}


// Kotlinx-datetime 轉換 helper（如果需要）
private fun java.time.LocalDate.toKotlinLocalDate(): LocalDate =
    LocalDate(year, monthValue, dayOfMonth)