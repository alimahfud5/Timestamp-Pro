package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedPhotoRepository
import com.example.data.model.CropAspectRatio
import com.example.data.model.NormalizedCropRect
import com.example.data.model.StampConfig
import com.example.data.model.TimestampSource
import com.example.util.BitmapUtils
import com.example.util.CropUtils
import com.example.util.ExifHelper
import com.example.util.ExtractedExifData
import com.example.util.LocationHelper
import com.example.util.PhotoSaver
import com.example.util.SaveResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val selectedPhotoUri: Uri? = null,
    val sourcePreviewBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val logoBitmap: Bitmap? = null,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val cropAspectRatio: CropAspectRatio = CropAspectRatio.ORIGINAL,
    val cropRect: NormalizedCropRect = NormalizedCropRect(),
    val config: StampConfig = StampConfig.DEFAULT,
    val canUndo: Boolean = false,
    val isGpsLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
    val exifData: ExtractedExifData? = null,
    val statusMessage: String? = null
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<StampConfig>()
    private var loadedLogoUri: String? = null

    fun syncLogoBitmap(context: Context) {
        val targetUri = _uiState.value.config.logoUri
        if (targetUri == null) {
            loadedLogoUri = null
            if (_uiState.value.logoBitmap != null) {
                _uiState.update { it.copy(logoBitmap = null) }
            }
            return
        }
        val existing = _uiState.value.logoBitmap
        if (loadedLogoUri == targetUri && existing != null && !existing.isRecycled) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = BitmapUtils.loadLogoBitmap(context, targetUri)
            loadedLogoUri = if (bmp != null) targetUri else null
            _uiState.update { it.copy(logoBitmap = bmp) }
        }
    }

    fun restoreDefaultLogo(context: Context) {
        val current = _uiState.value.config
        val updated = current.copy(logoUri = StampConfig.DEFAULT_LOGO_URI)
        updateConfig(updated)
        loadedLogoUri = null
        syncLogoBitmap(context)
        _uiState.update { it.copy(statusMessage = "Logo default dikembalikan.") }
    }

    fun onPhotoSelected(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(statusMessage = "Membaca data foto & EXIF...") }
            val exif = ExifHelper.extractExif(context, uri)

            // Load oriented preview bitmap (scaled to max 1600px for responsiveness)
            val bitmap = BitmapUtils.loadOrientedBitmap(
                context = context,
                uri = uri,
                maxDimension = 1600,
                orientationDegrees = exif.orientationDegrees
            )

            undoStack.clear()

            val initialConfig = _uiState.value.config.copy(
                originalExifTimestamp = exif.originalDateTimeMillis,
                source = if (exif.originalDateTimeMillis != null) TimestampSource.ORIGINAL_EXIF else TimestampSource.CURRENT,
                latitude = exif.latitude ?: _uiState.value.config.latitude,
                longitude = exif.longitude ?: _uiState.value.config.longitude,
                altitude = exif.altitude ?: _uiState.value.config.altitude
            )

            // If EXIF had GPS, try to resolve address
            var resolvedAddress = initialConfig.locationName
            if (exif.latitude != null && exif.longitude != null && resolvedAddress.isBlank()) {
                val addr = LocationHelper.resolveAddress(context, exif.latitude, exif.longitude)
                if (addr != null) resolvedAddress = addr
            }

            val finalConfig = initialConfig.copy(locationName = resolvedAddress)

            val previousState = _uiState.value
            val ratioToApply = if (previousState.sourcePreviewBitmap == null) {
                previousState.cropAspectRatio
            } else {
                CropAspectRatio.ORIGINAL
            }
            val initialCropRect = if (
                bitmap != null &&
                ratioToApply != CropAspectRatio.ORIGINAL &&
                ratioToApply != CropAspectRatio.FREE
            ) {
                CropUtils.computeCenteredCropRect(bitmap.width, bitmap.height, ratioToApply)
            } else {
                NormalizedCropRect()
            }
            val initialPreviewBitmap = if (
                bitmap != null &&
                initialCropRect != NormalizedCropRect()
            ) {
                CropUtils.cropBitmap(bitmap, initialCropRect)
            } else {
                bitmap
            }

            _uiState.update {
                it.copy(
                    selectedPhotoUri = uri,
                    sourcePreviewBitmap = bitmap,
                    previewBitmap = initialPreviewBitmap,
                    originalWidth = bitmap?.width ?: 0,
                    originalHeight = bitmap?.height ?: 0,
                    cropAspectRatio = ratioToApply,
                    cropRect = initialCropRect,
                    exifData = exif,
                    config = finalConfig,
                    canUndo = false,
                    statusMessage = null
                )
            }

            // If location/GPS still blank after reading photo EXIF, fetch current device GPS
            if ((finalConfig.latitude == null || finalConfig.locationName.isBlank()) &&
                LocationHelper.hasLocationPermission(context)
            ) {
                fetchCurrentGps(context, silent = true)
            }
        }
    }

    fun updateConfig(newConfig: StampConfig) {
        val current = _uiState.value.config
        if (current != newConfig) {
            undoStack.add(current)
            if (undoStack.size > 20) {
                undoStack.removeAt(0)
            }
            _uiState.update {
                it.copy(
                    config = newConfig,
                    canUndo = undoStack.isNotEmpty()
                )
            }
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _uiState.update {
                it.copy(
                    config = previous,
                    canUndo = undoStack.isNotEmpty()
                )
            }
        }
    }

    fun resetConfig(context: Context? = null) {
        val exifTime = _uiState.value.exifData?.originalDateTimeMillis
        val defaultConfig = StampConfig.DEFAULT.copy(
            originalExifTimestamp = exifTime,
            source = if (exifTime != null) TimestampSource.ORIGINAL_EXIF else TimestampSource.CURRENT
        )
        updateConfig(defaultConfig)
        loadedLogoUri = null
        if (context != null) {
            syncLogoBitmap(context)
            if (LocationHelper.hasLocationPermission(context)) {
                fetchCurrentGps(context, silent = true)
            }
        }
    }

    fun applyPreset(preset: StampConfig) {
        val exifTime = _uiState.value.exifData?.originalDateTimeMillis
        val currentConfig = _uiState.value.config
        val merged = preset.copy(
            originalExifTimestamp = exifTime,
            latitude = currentConfig.latitude ?: preset.latitude,
            longitude = currentConfig.longitude ?: preset.longitude,
            altitude = currentConfig.altitude ?: preset.altitude,
            locationName = if (currentConfig.locationName.isNotBlank()) currentConfig.locationName else preset.locationName,
            logoUri = currentConfig.logoUri ?: preset.logoUri,
            logoPosition = if (currentConfig.logoUri != null) currentConfig.logoPosition else preset.logoPosition,
            logoSizePercent = if (currentConfig.logoUri != null) currentConfig.logoSizePercent else preset.logoSizePercent
        )
        updateConfig(merged)
    }

    fun onLogoSelected(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = BitmapUtils.saveLogoToInternalStorage(context, uri, maxDimension = 1024)
            if (saved != null) {
                val (internalUri, bmp) = saved
                val uriStr = internalUri.toString()
                val updatedConfig = _uiState.value.config.copy(logoUri = uriStr)
                undoStack.add(_uiState.value.config)
                if (undoStack.size > 20) undoStack.removeAt(0)
                loadedLogoUri = uriStr
                _uiState.update {
                    it.copy(
                        logoBitmap = bmp,
                        config = updatedConfig,
                        canUndo = undoStack.isNotEmpty(),
                        statusMessage = "Logo berhasil ditambahkan!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(statusMessage = "Gagal memuat gambar logo.")
                }
            }
        }
    }

    fun removeLogo() {
        val updatedConfig = _uiState.value.config.copy(logoUri = null)
        loadedLogoUri = null
        updateConfig(updatedConfig)
        _uiState.update { it.copy(logoBitmap = null, statusMessage = "Logo dihapus.") }
    }

    fun autoFetchDefaultGpsIfPermitted(context: Context) {
        val current = _uiState.value.config
        if ((current.latitude == null || current.locationName.isBlank()) &&
            LocationHelper.hasLocationPermission(context) &&
            !_uiState.value.isGpsLoading
        ) {
            fetchCurrentGps(context, silent = true)
        }
    }

    fun fetchCurrentGps(context: Context, silent: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGpsLoading = true) }
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                val updated = _uiState.value.config.copy(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = loc.altitude,
                    locationName = loc.addressName ?: _uiState.value.config.locationName,
                    includeGps = true,
                    includeLocation = true
                )
                if (silent) {
                    _uiState.update { it.copy(config = updated, isGpsLoading = false) }
                } else {
                    updateConfig(updated)
                    _uiState.update { it.copy(isGpsLoading = false, statusMessage = "GPS berhasil diperbarui!") }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isGpsLoading = false,
                        statusMessage = if (silent) null else "Tidak dapat mendeteksi lokasi. Pastikan GPS aktif."
                    )
                }
            }
        }
    }

    fun applyCropAspectRatio(aspectRatio: CropAspectRatio) {
        val source = _uiState.value.sourcePreviewBitmap
        if (source == null) {
            _uiState.update {
                it.copy(
                    cropAspectRatio = aspectRatio,
                    cropRect = NormalizedCropRect()
                )
            }
            return
        }
        val rect = CropUtils.computeCenteredCropRect(source.width, source.height, aspectRatio)
        applyCrop(aspectRatio, rect)
    }

    fun applyCrop(aspectRatio: CropAspectRatio, rect: NormalizedCropRect) {
        val source = _uiState.value.sourcePreviewBitmap ?: return
        val cropped = CropUtils.cropBitmap(source, rect)
        _uiState.update {
            it.copy(
                previewBitmap = cropped,
                cropAspectRatio = aspectRatio,
                cropRect = rect
            )
        }
    }

    fun resetCrop() {
        val source = _uiState.value.sourcePreviewBitmap ?: return
        _uiState.update {
            it.copy(
                previewBitmap = source,
                cropAspectRatio = CropAspectRatio.ORIGINAL,
                cropRect = NormalizedCropRect()
            )
        }
    }

    fun savePhoto(
        context: Context,
        repository: SavedPhotoRepository,
        quality: Int = 95,
        preserveExif: Boolean = true
    ) {
        val uri = _uiState.value.selectedPhotoUri ?: return
        val activeCropRect = _uiState.value.cropRect
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, statusMessage = "Memproses dan menyimpan foto dengan resolusi penuh...") }
            val result = PhotoSaver.saveStampedPhoto(
                context = context,
                originalUri = uri,
                config = _uiState.value.config,
                repository = repository,
                qualityPercent = quality,
                preserveExif = preserveExif,
                cropRect = activeCropRect
            )
            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveResult = result,
                    statusMessage = if (result.success) "Foto berhasil disimpan ke Galeri!" else result.errorMessage
                )
            }
        }
    }

    fun dismissSaveResult() {
        _uiState.update { it.copy(saveResult = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
