package com.gary.communitycatdb.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object FileUtil {
    /**
     * 將選取的 Uri 圖片 Resize 並儲存到 App 內部空間
     */
    fun savePhotoToInternalStorage(context: Context, uri: Uri): String {
        val fileName = "cat_photo_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            // --- 加入 Resize 邏輯 (建議選用) ---
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(input)
            
            // 設定最大寬度為 1024px，高度等比例縮放
            val targetWidth = 1024
            val ratio = originalBitmap.height.toFloat() / originalBitmap.width.toFloat()
            val targetHeight = (targetWidth * ratio).toInt()
            
            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(
                originalBitmap, targetWidth, targetHeight, true
            )

            // 以 80% 質量儲存為 JPEG
            java.io.FileOutputStream(file).use { output ->
                resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output)
            }
            
            // 釋放記憶體
            originalBitmap.recycle()
            resizedBitmap.recycle()
        }
        return file.absolutePath
    }
}
