package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.example.data.local.SavedPhotoEntity
import com.example.data.local.SavedPhotoRepository
import com.example.data.model.NormalizedCropRect
import com.example.data.model.StampConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SaveResult(
    val success: Boolean,
    val savedUri: Uri? = null,
    val errorMessage: String? = null,
    val entity: SavedPhotoEntity? = null
)

object PhotoSaver {

    suspend fun saveStampedPhoto(
        context: Context,
        originalUri: Uri,
        config: StampConfig,
        repository: SavedPhotoRepository,
        qualityPercent: Int = 95,
        preserveExif: Boolean = true,
        cropRect: NormalizedCropRect = NormalizedCropRect()
    ): SaveResult = withContext(Dispatchers.IO) {
        try {
            // 1. Extract original EXIF info
            val sourceExif = ExifHelper.extractExif(context, originalUri)

            // 2. Load full-resolution bitmap with correct orientation
            val fullBitmap = BitmapUtils.loadOrientedBitmap(
                context = context,
                uri = originalUri,
                maxDimension = null, // Load full resolution
                orientationDegrees = sourceExif.orientationDegrees
            ) ?: return@withContext SaveResult(false, errorMessage = "Gagal memuat foto resolusi penuh.")

            // 2b. Apply crop at full resolution if cropped
            val croppedBitmap = CropUtils.cropBitmap(fullBitmap, cropRect)
            if (croppedBitmap != fullBitmap) {
                fullBitmap.recycle()
            }

            // 3. Load default or custom logo and render stamp on cropped full-resolution bitmap
            val logoBitmap = BitmapUtils.loadLogoBitmap(context, config.logoUri)
            val stampedBitmap = StampRenderer.renderStampOnBitmap(croppedBitmap, config, context, logoBitmap)
            if (logoBitmap != null && !logoBitmap.isRecycled) {
                logoBitmap.recycle()
            }
            if (stampedBitmap != croppedBitmap) {
                croppedBitmap.recycle()
            }

            val timestampMillis = System.currentTimeMillis()
            val fileName = "TIMESTAMP_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestampMillis))}.jpg"

            // 4. Insert into MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TimestampPhotoPro")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val savedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext SaveResult(false, errorMessage = "Gagal membuat entri penyimpanan MediaStore.")

            var writeSuccess = false
            resolver.openOutputStream(savedUri)?.use { outStream: OutputStream ->
                writeSuccess = stampedBitmap.compress(Bitmap.CompressFormat.JPEG, qualityPercent.coerceIn(80, 100), outStream)
                outStream.flush()
            }

            if (!writeSuccess) {
                resolver.delete(savedUri, null, null)
                return@withContext SaveResult(false, errorMessage = "Gagal menyimpan berkas gambar ke disk.")
            }

            // 5. Clear IS_PENDING on API 29+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(savedUri, contentValues, null, null)
            }

            // 6. Write EXIF metadata to the newly saved file
            if (preserveExif) {
                try {
                    resolver.openFileDescriptor(savedUri, "rw")?.use { pfd ->
                        val destinationExif = ExifInterface(pfd.fileDescriptor)
                        ExifHelper.copyExifToSavedFile(
                            sourceExif = sourceExif,
                            destinationExif = destinationExif,
                            stampedTimeMillis = config.getResolvedTimestamp(),
                            overrideLat = config.latitude,
                            overrideLon = config.longitude,
                            overrideAlt = config.altitude
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val formattedTime = try {
                SimpleDateFormat(config.dateFormatPattern, Locale("id", "ID")).format(Date(config.getResolvedTimestamp()))
            } catch (e: Exception) {
                Date(config.getResolvedTimestamp()).toString()
            }

            val gpsCoordsText = if (config.includeGps && (config.latitude != null || config.longitude != null)) {
                "${config.latitude}, ${config.longitude}"
            } else null

            // 7. Save to Room database
            val entity = SavedPhotoEntity(
                savedUri = savedUri.toString(),
                originalUri = originalUri.toString(),
                timestampMillis = timestampMillis,
                formattedTimestamp = formattedTime,
                locationText = if (config.includeLocation) config.locationName.takeIf { it.isNotBlank() } else null,
                gpsCoordsText = gpsCoordsText,
                activityName = if (config.includeActivity) config.activityName.takeIf { it.isNotBlank() } else null,
                officerName = if (config.includeOfficer) config.officerName.takeIf { it.isNotBlank() } else null,
                notes = if (config.includeNotes) config.notes.takeIf { it.isNotBlank() } else null,
                width = stampedBitmap.width,
                height = stampedBitmap.height
            )
            repository.insert(entity)

            stampedBitmap.recycle()

            SaveResult(
                success = true,
                savedUri = savedUri,
                entity = entity
            )
        } catch (e: Exception) {
            e.printStackTrace()
            SaveResult(false, errorMessage = e.localizedMessage ?: "Terjadi kesalahan saat menyimpan foto.")
        }
    }
}
