package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TempCameraImage(
    val uri: Uri,
    val file: File
)

object CameraHelper {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun createTempCameraImage(context: Context): TempCameraImage {
        val cameraDir = File(context.cacheDir, "camera_photos").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFile = File.createTempFile("CAM_${timestamp}_", ".jpg", cameraDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        return TempCameraImage(uri = uri, file = imageFile)
    }
}
