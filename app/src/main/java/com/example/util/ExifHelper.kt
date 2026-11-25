package com.example.util

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

data class ExtractedExifData(
    val originalDateTimeMillis: Long? = null,
    val formattedDateTime: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val orientationDegrees: Int = 0
)

object ExifHelper {
    private val EXIF_DATE_FORMAT = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)

    fun extractExif(context: Context, uri: Uri): ExtractedExifData {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                inputStream.use { stream ->
                    val exif = ExifInterface(stream)
                    val dateString = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)

                    val dateMillis = dateString?.let {
                        try {
                            EXIF_DATE_FORMAT.parse(it)?.time
                        } catch (e: Exception) {
                            null
                        }
                    }

                    val latLong = exif.latLong
                    val lat = latLong?.get(0)
                    val lon = latLong?.get(1)
                    val alt = exif.getAltitude(0.0).takeIf { it != 0.0 }

                    val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                    val model = exif.getAttribute(ExifInterface.TAG_MODEL)

                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }

                    ExtractedExifData(
                        originalDateTimeMillis = dateMillis,
                        formattedDateTime = dateString,
                        latitude = lat,
                        longitude = lon,
                        altitude = alt,
                        cameraMake = make,
                        cameraModel = model,
                        orientationDegrees = rotationDegrees
                    )
                }
            } else {
                ExtractedExifData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ExtractedExifData()
        }
    }

    fun copyExifToSavedFile(
        sourceExif: ExtractedExifData,
        destinationExif: ExifInterface,
        stampedTimeMillis: Long,
        overrideLat: Double?,
        overrideLon: Double?,
        overrideAlt: Double?
    ) {
        try {
            // Write camera make/model if present
            sourceExif.cameraMake?.let { destinationExif.setAttribute(ExifInterface.TAG_MAKE, it) }
            sourceExif.cameraModel?.let { destinationExif.setAttribute(ExifInterface.TAG_MODEL, it) }

            // Write formatted date
            val formattedDate = EXIF_DATE_FORMAT.format(stampedTimeMillis)
            destinationExif.setAttribute(ExifInterface.TAG_DATETIME, formattedDate)
            destinationExif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedDate)
            destinationExif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedDate)

            // Write GPS
            val lat = overrideLat ?: sourceExif.latitude
            val lon = overrideLon ?: sourceExif.longitude
            if (lat != null && lon != null) {
                destinationExif.setLatLong(lat, lon)
            }
            val alt = overrideAlt ?: sourceExif.altitude
            if (alt != null) {
                destinationExif.setAltitude(alt)
            }

            // Software tag
            destinationExif.setAttribute(ExifInterface.TAG_SOFTWARE, "TIMESTAMP PHOTO PRO")
            destinationExif.saveAttributes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
