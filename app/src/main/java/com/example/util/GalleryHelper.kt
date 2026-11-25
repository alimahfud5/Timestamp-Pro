package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

object GalleryHelper {

    fun openDeviceGallery(context: Context) {
        try {
            val galleryIntent = Intent.makeMainSelectorActivity(
                Intent.ACTION_MAIN,
                Intent.CATEGORY_APP_GALLERY
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(galleryIntent)
        } catch (e: Exception) {
            try {
                val mediaIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mediaIntent)
            } catch (e2: Exception) {
                Toast.makeText(
                    context,
                    "Tidak dapat membuka aplikasi Galeri perangkat",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun openImageInGallery(context: Context, imageUri: Uri) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(imageUri, "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            openDeviceGallery(context)
        }
    }
}
