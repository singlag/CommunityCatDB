package com.gary.communitycatdb.util

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
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
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.io.FileInputStream


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
     * 匯出整個資料庫成 JSON + jpg檔案
     */
    suspend fun exportDatabase(
        context: Context,
        cats: List<Cat>,
        repository: CatRepository
    ): File? = withContext(Dispatchers.IO) {
        try {
            val backupDir = context.getExternalFilesDir("backup") ?: return@withContext null
            backupDir.mkdirs()

            // 1. 產生 JSON 內容
            val exportList = cats.map { cat ->
                val locations = repository.getLocations(cat.name)
                ExportData(cat, locations)
            }

            val jsonString = json.encodeToString(exportList)
            val jsonFile = File(backupDir, "data.json")
            jsonFile.writeText(jsonString)

            // 2. 準備 ZIP 檔案
            val zipFile = File(backupDir, "CommunityCat_FullBackup_${System.currentTimeMillis()}.zip")

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                // 加入 JSON 檔案
                addToZip(jsonFile, "data.json", zos)

                // 3. 加入所有在 Cat 資料中提到的照片
                cats.forEach { cat ->
                    cat.photoPath?.let { path ->
                        val photoFile = File(path)
                        if (photoFile.exists()) {
                            // 在 ZIP 內建立 photos 檔案夾
                            addToZip(photoFile, "photos/${photoFile.name}", zos)
                        }
                    }
                }
            }

            // 刪除暫存的 JSON
            jsonFile.delete()
            zipFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addToZip(file: File, fileName: String, zos: ZipOutputStream) {
        FileInputStream(file).use { fis ->
            val entry = ZipEntry(fileName)
            zos.putNextEntry(entry)
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }


    /**
     * 從選取的 ZIP 檔案匯入資料
     * @param uri 使用者從檔案選擇器選取的 JSON Uri
     * @return 成功匯入的貓隻數量
     */
    suspend fun importDatabase(
        context: Context,
        uri: Uri,
        repository: CatRepository
    ): Int = withContext(Dispatchers.IO) {
        try {
            // 讀取 ZIP 內容
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext -1
            val tempDir = File(context.cacheDir, "import_temp")
            tempDir.deleteRecursively()
            tempDir.mkdirs()

            // 1. 解壓縮所有檔案到暫存區
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(tempDir, entry.name)
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { zis.copyTo(it) }
                    entry = zis.nextEntry
                }
            }


            // 2. 讀取 JSON 並更新照片路徑
            val jsonFile = File(tempDir, "data.json")
            val jsonString = jsonFile.readText()
            val importList: List<ExportData> = json.decodeFromString(jsonString)

            var count = 0
            importList.forEach { data ->
                val oldCat = data.cat
                var newPhotoPath = oldCat.photoPath

                // 如果有照片，搬移到正式的 filesDir 並修正路徑
                if (oldCat.photoPath != null) {
                    val oldPhotoFile = File(tempDir, "photos/${File(oldCat.photoPath).name}")
                    if (oldPhotoFile.exists()) {
                        val targetFile = File(context.filesDir, oldPhotoFile.name)
                        oldPhotoFile.copyTo(targetFile, overwrite = true)
                        newPhotoPath = targetFile.absolutePath
                    }
                }


                // --- 先刪除該貓隻的所有舊位置 ---
                repository.deleteLocationsByCatName(oldCat.name)

                // 儲存更新路徑後的貓隻
                repository.saveCat(oldCat.copy(photoPath = newPhotoPath))
                data.locations.forEach { repository.addLocation(it) }
                count++
            }

            tempDir.deleteRecursively()
            count

        } catch (e: Exception) {
            e.printStackTrace()
            -1 // 失敗返回 -1
        }
    }



    /**
     * 將檔案從私有目錄移動到 Downloads 資料夾，並刪除原始檔案
     */
    fun moveFileToDownloads(context: Context, sourceFile: File): File? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetFile = File(downloadsDir, sourceFile.name)

            // 1. 執行複製
            sourceFile.copyTo(targetFile, overwrite = true)

            // 2. 通知系統掃描新檔案（讓用戶能在檔案瀏覽器立即看到）
            MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), null, null)

            // 3. 複製成功後，刪除原始私有目錄的檔案
            if (sourceFile.exists()) {
                sourceFile.delete()
            }

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}


