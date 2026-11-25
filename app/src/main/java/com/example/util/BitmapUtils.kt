package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

object BitmapUtils {

    private fun openUriInputStream(context: Context, uri: Uri): InputStream? {
        return if (uri.scheme == "file" && !uri.path.isNullOrBlank()) {
            FileInputStream(File(uri.path!!))
        } else {
            context.contentResolver.openInputStream(uri)
        }
    }

    fun loadOrientedBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int? = null,
        orientationDegrees: Int = 0
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                if (maxDimension != null) {
                    inJustDecodeBounds = true
                    openUriInputStream(context, uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, this)
                    }
                    val srcWidth = outWidth
                    val srcHeight = outHeight
                    var sampleSize = 1
                    val maxSide = max(srcWidth, srcHeight)
                    while (maxSide / (sampleSize * 2) >= maxDimension) {
                        sampleSize *= 2
                    }
                    inSampleSize = sampleSize
                    inJustDecodeBounds = false
                }
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var bitmap: Bitmap? = openUriInputStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (bitmap != null && orientationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(orientationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies and downscales a user-picked logo into app-private storage as PNG
     * so alpha transparency is preserved and URI read permissions never expire.
     */
    fun saveLogoToInternalStorage(
        context: Context,
        sourceUri: Uri,
        maxDimension: Int = 1024
    ): Pair<Uri, Bitmap>? {
        return try {
            val exif = ExifHelper.extractExif(context, sourceUri)
            val decoded = loadOrientedBitmap(
                context = context,
                uri = sourceUri,
                maxDimension = maxDimension,
                orientationDegrees = exif.orientationDegrees
            ) ?: return null

            val finalBitmap = if (max(decoded.width, decoded.height) > maxDimension) {
                val scale = maxDimension.toFloat() / max(decoded.width, decoded.height).toFloat()
                val targetW = (decoded.width * scale).roundToInt().coerceAtLeast(1)
                val targetH = (decoded.height * scale).roundToInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                if (scaled != decoded) decoded.recycle()
                scaled
            } else {
                decoded
            }

            val logoFile = File(context.filesDir, "custom_stamp_logo.png")
            FileOutputStream(logoFile).use { fos ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            Uri.fromFile(logoFile) to finalBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
