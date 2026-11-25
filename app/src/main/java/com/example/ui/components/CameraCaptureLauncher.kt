package com.example.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.util.CameraHelper
import java.io.File

@Composable
fun rememberCameraCaptureAction(
    onPhotoCaptured: (Uri) -> Unit,
    onPermissionDenied: () -> Unit = {},
    onLaunchError: (String) -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    var pendingUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingFilePath by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = pendingUriString?.let { Uri.parse(it) }
        val tempFile = pendingFilePath?.let { File(it) }
        pendingUriString = null
        pendingFilePath = null

        if (success && capturedUri != null) {
            onPhotoCaptured(capturedUri)
        } else {
            tempFile?.let {
                if (it.exists() && it.length() == 0L) {
                    it.delete()
                }
            }
        }
    }

    fun launchCameraIntent() {
        try {
            val temp = CameraHelper.createTempCameraImage(context)
            pendingUriString = temp.uri.toString()
            pendingFilePath = temp.file.absolutePath
            takePictureLauncher.launch(temp.uri)
        } catch (e: ActivityNotFoundException) {
            pendingUriString = null
            pendingFilePath = null
            onLaunchError("Aplikasi kamera tidak ditemukan di perangkat ini.")
        } catch (e: Exception) {
            pendingUriString = null
            pendingFilePath = null
            onLaunchError(e.localizedMessage ?: "Gagal membuka kamera perangkat.")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraIntent()
        } else {
            onPermissionDenied()
        }
    }

    return {
        if (CameraHelper.hasCameraPermission(context)) {
            launchCameraIntent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
