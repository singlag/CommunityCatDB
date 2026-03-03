package com.gary.communitycatdb.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.gary.communitycatdb.data.model.Cat
import com.gary.communitycatdb.data.model.CatLocation
import com.gary.communitycatdb.data.repository.CatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

/**
 * 社區貓資料庫 Export / Import 工具類
 * 支援 JSON 備份與還原（包含所有貓隻 + 多位置資料）
 */
object ExportImportUtil {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Serializable
    data class ExportData(
        val cat: Cat,
        val locations: List<CatLocation>
    )

    /**
     * 匯出整個資料庫成 JSON 檔案，並返回可分享的 Uri
     * @return 可直接用於 Intent.ACTION_SEND 的 Uri
     */
    suspend fun exportDatabase(
        context: Context,
        cats: List<Cat>,
        repository: CatRepository
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportList = cats.map { cat ->
                val locations = repository.getLocations(cat.name)
                ExportData(cat, locations)
            }

            val jsonString = json.encodeToString(exportList)

            // 儲存到外部檔案目錄（App 專屬，不會被清除）
            val backupDir = context.getExternalFilesDir(null) ?: return@withContext null
            val fileName = "community_cats_backup_${System.currentTimeMillis()}.json"
            val backupFile = File(backupDir, fileName)

            FileOutputStream(backupFile).use { it.write(jsonString.toByteArray()) }

            // 使用 FileProvider 產生安全 Uri（必須在 manifest 設定）
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 從選取的 JSON 檔案匯入資料
     * @param uri 使用者從檔案選擇器選取的 JSON Uri
     * @return 成功匯入的貓隻數量
     */
    suspend fun importDatabase(
        context: Context,
        uri: Uri,
        repository: CatRepository
    ): Int = withContext(Dispatchers.IO) {
        try {
            // 讀取 JSON 內容
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("無法開啟檔案")

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            val importList: List<ExportData> = json.decodeFromString(jsonString)

            var importedCount = 0

            importList.forEach { data ->
                // 儲存貓隻資料（自動覆蓋同名）
                repository.saveCat(data.cat)
                // 儲存所有位置
                data.locations.forEach { location ->
                    repository.addLocation(location)
                }
                importedCount++
            }

            importedCount
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // 失敗返回 -1
        }
    }

    /**
     * 建立分享 Intent（呼叫後可直接 startActivity）
     */
    fun createShareIntent(context: Context, uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}