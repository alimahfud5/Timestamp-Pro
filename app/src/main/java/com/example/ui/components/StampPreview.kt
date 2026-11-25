package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StampConfig
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.util.StampRenderer

@Composable
fun StampPreview(
    previewBitmap: Bitmap?,
    config: StampConfig,
    logoBitmap: Bitmap? = null,
    onPickPhotoClick: () -> Unit,
    onOpenCameraClick: () -> Unit = {},
    onCropPhotoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (previewBitmap == null) {
        // Empty state placeholder
        EmptyPhotoSelectorCard(
            onPickPhotoClick = onPickPhotoClick,
            onOpenCameraClick = onOpenCameraClick,
            modifier = modifier
        )
    } else {
        // Active photo with live stamp overlay
        ActivePhotoPreview(
            bitmap = previewBitmap,
            config = config,
            logoBitmap = logoBitmap,
            onChangePhotoClick = onPickPhotoClick,
            onOpenCameraClick = onOpenCameraClick,
            onCropPhotoClick = onCropPhotoClick,
            modifier = modifier
        )
    }
}

@Composable
private fun EmptyPhotoSelectorCard(
    onPickPhotoClick: () -> Unit,
    onOpenCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(CyanAccent.copy(alpha = 0.6f), Navy700)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onPickPhotoClick() }
            .testTag("empty_photo_picker_card"),
        color = Navy800.copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CyanAccent.copy(alpha = 0.25f), Navy700)
                        )
                    )
                    .border(1.5.dp, CyanAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Pilih Foto",
                    tint = CyanAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PILIH FOTO DARI GALERI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tambahkan stempel tanggal, jam, GPS, nama kegiatan & petugas pada foto dokumentasi Anda",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SlateTextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPickPhotoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Navy900
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_select_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pilih Foto",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onOpenCameraClick,
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyanBright
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_open_camera_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Masuk Camera",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Masuk Camera",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivePhotoPreview(
    bitmap: Bitmap,
    config: StampConfig,
    logoBitmap: Bitmap?,
    onChangePhotoClick: () -> Unit,
    onOpenCameraClick: () -> Unit,
    onCropPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat().coerceAtLeast(1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Top Bar over Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Navy800.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${bitmap.width}×${bitmap.height}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SlateTextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCropPhotoClick,
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Navy800,
                        contentColor = CyanBright
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_active_crop_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = "Edit Crop Gambar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Crop",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                OutlinedButton(
                    onClick = onOpenCameraClick,
                    border = BorderStroke(1.dp, Navy700),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Navy800,
                        contentColor = CyanBright
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_active_camera_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Masuk Camera",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Camera",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Button(
                    onClick = onChangePhotoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy700,
                        contentColor = CyanBright
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_change_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Ganti Foto",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Ganti",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        // Photo & Stamp Canvas Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Navy700, RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio.coerceIn(0.5f, 2.5f))
            ) {
                // Background image
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Foto yang diedit",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Live dynamic stamp overlay drawn directly onto the viewport canvas!
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("live_stamp_canvas")
                ) {
                    drawIntoCanvas { canvas ->
                        // Pass native canvas and actual viewport dimensions
                        val targetW = size.width.toInt()
                        val targetH = size.height.toInt()
                        if (targetW > 0 && targetH > 0) {
                            StampRenderer.drawStamp(
                                canvas = canvas.nativeCanvas,
                                targetWidth = targetW,
                                targetHeight = targetH,
                                config = config,
                                context = context,
                                logoBitmap = logoBitmap
                            )
                        }
                    }
                }
            }
        }
    }
}
