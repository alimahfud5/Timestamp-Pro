package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropAspectRatio
import com.example.data.model.NormalizedCropRect
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.util.CropDragHandle
import com.example.util.CropUtils
import kotlin.math.hypot
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropSection(
    hasPhoto: Boolean,
    sourceWidth: Int,
    sourceHeight: Int,
    currentWidth: Int,
    currentHeight: Int,
    selectedAspectRatio: CropAspectRatio,
    onSelectAspectRatio: (CropAspectRatio) -> Unit,
    onOpenInteractiveCrop: () -> Unit,
    onResetCrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Edit & Crop Gambar",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                        )
                        Text(
                            text = if (hasPhoto) {
                                "Aktif: ${currentWidth} × ${currentHeight} px (Asli: ${sourceWidth} × ${sourceHeight} px)"
                            } else if (selectedAspectRatio != CropAspectRatio.ORIGINAL) {
                                "Rasio terpilih: ${selectedAspectRatio.displayName} • Otomatis diterapkan setelah pilih foto / kamera"
                            } else {
                                "Pilih foto atau ambil dari kamera, lalu pilih rasio crop di bawah"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenInteractiveCrop,
                        enabled = hasPhoto,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Navy900,
                            disabledContainerColor = Navy700,
                            disabledContentColor = SlateTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_open_interactive_crop")
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sesuaikan Crop", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onResetCrop,
                        enabled = hasPhoto,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_reset_crop")
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Reset ke Asli",
                            tint = if (hasPhoto) CyanBright else SlateTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Asli",
                            color = if (hasPhoto) CyanBright else SlateTextSecondary
                        )
                    }
                }
            }
        }

        Text(
            text = "RASIO CROP GAMBAR",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CropAspectRatio.values().forEach { ratio ->
                val isSelected = selectedAspectRatio == ratio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectAspectRatio(ratio) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("crop_ratio_${ratio.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ratio.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropImageDialog(
    sourceBitmap: Bitmap,
    initialAspectRatio: CropAspectRatio,
    initialCropRect: NormalizedCropRect,
    onDismiss: () -> Unit,
    onApplyCrop: (CropAspectRatio, NormalizedCropRect) -> Unit
) {
    val imageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }
    var selectedRatio by remember { mutableStateOf(initialAspectRatio) }
    var cropRect by remember { mutableStateOf(initialCropRect) }

    val croppedWidth = ((cropRect.width * sourceBitmap.width).roundToInt()).coerceAtLeast(1)
    val croppedHeight = ((cropRect.height * sourceBitmap.height).roundToInt()).coerceAtLeast(1)

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Navy900
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = SlateTextPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EDIT & CROP GAMBAR",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                    )
                    Text(
                        text = "${croppedWidth} × ${croppedHeight} px • ${selectedRatio.displayName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(color = CyanBright)
                    )
                }

                IconButton(
                    onClick = {
                        selectedRatio = CropAspectRatio.ORIGINAL
                        cropRect = NormalizedCropRect(0f, 0f, 1f, 1f)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Crop",
                        tint = SlateTextSecondary
                    )
                }

                FilledIconButton(
                    onClick = { onApplyCrop(selectedRatio, cropRect) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CyanAccent,
                        contentColor = Navy900
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_apply_crop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terapkan Crop",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

                // Interactive Image & Crop Frame Canvas
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val containerW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                    val containerH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
                    val imgRatio = sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat().coerceAtLeast(1f)
                    val containerRatio = containerW / containerH

                    val (imgDrawW, imgDrawH) = if (imgRatio > containerRatio) {
                        containerW to (containerW / imgRatio)
                    } else {
                        (containerH * imgRatio) to containerH
                    }
                    val imgLeft = (containerW - imgDrawW) / 2f
                    val imgTop = (containerH - imgDrawH) / 2f

                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Foto yang dicrop",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    var activeHandle by remember { mutableStateOf<CropDragHandle?>(null) }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(sourceBitmap, selectedRatio) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        val rectL = imgLeft + cropRect.left * imgDrawW
                                        val rectT = imgTop + cropRect.top * imgDrawH
                                        val rectR = imgLeft + cropRect.right * imgDrawW
                                        val rectB = imgTop + cropRect.bottom * imgDrawH

                                        val touchRadius = 64f
                                        val dTL = hypot(startOffset.x - rectL, startOffset.y - rectT)
                                        val dTR = hypot(startOffset.x - rectR, startOffset.y - rectT)
                                        val dBL = hypot(startOffset.x - rectL, startOffset.y - rectB)
                                        val dBR = hypot(startOffset.x - rectR, startOffset.y - rectB)

                                        val minCornerDist = minOf(dTL, dTR, dBL, dBR)
                                        activeHandle = when {
                                            minCornerDist <= touchRadius && minCornerDist == dTL -> CropDragHandle.TOP_LEFT
                                            minCornerDist <= touchRadius && minCornerDist == dTR -> CropDragHandle.TOP_RIGHT
                                            minCornerDist <= touchRadius && minCornerDist == dBL -> CropDragHandle.BOTTOM_LEFT
                                            minCornerDist <= touchRadius && minCornerDist == dBR -> CropDragHandle.BOTTOM_RIGHT
                                            startOffset.x in rectL..rectR && startOffset.y in rectT..rectB -> CropDragHandle.MOVE
                                            else -> null
                                        }

                                        if (activeHandle != null &&
                                            activeHandle != CropDragHandle.MOVE &&
                                            selectedRatio == CropAspectRatio.ORIGINAL
                                        ) {
                                            selectedRatio = CropAspectRatio.FREE
                                        }
                                    },
                                    onDragEnd = { activeHandle = null },
                                    onDragCancel = { activeHandle = null },
                                    onDrag = { change, dragAmount ->
                                        val handle = activeHandle ?: return@detectDragGestures
                                        change.consume()
                                        val dxNorm = dragAmount.x / imgDrawW
                                        val dyNorm = dragAmount.y / imgDrawH
                                        cropRect = CropUtils.updateCropRectByDrag(
                                            current = cropRect,
                                            handle = handle,
                                            dxNorm = dxNorm,
                                            dyNorm = dyNorm,
                                            imageWidth = sourceBitmap.width,
                                            imageHeight = sourceBitmap.height,
                                            aspectRatio = selectedRatio
                                        )
                                    }
                                )
                            }
                            .testTag("interactive_crop_canvas")
                    ) {
                        val rectL = imgLeft + cropRect.left * imgDrawW
                        val rectT = imgTop + cropRect.top * imgDrawH
                        val rectR = imgLeft + cropRect.right * imgDrawW
                        val rectB = imgTop + cropRect.bottom * imgDrawH
                        val rectW = (rectR - rectL).coerceAtLeast(1f)
                        val rectH = (rectB - rectT).coerceAtLeast(1f)

                        val scrimColor = Color.Black.copy(alpha = 0.62f)
                        // Top scrim
                        drawRect(scrimColor, topLeft = Offset(imgLeft, imgTop), size = Size(imgDrawW, (rectT - imgTop).coerceAtLeast(0f)))
                        // Bottom scrim
                        drawRect(scrimColor, topLeft = Offset(imgLeft, rectB), size = Size(imgDrawW, (imgTop + imgDrawH - rectB).coerceAtLeast(0f)))
                        // Left scrim
                        drawRect(scrimColor, topLeft = Offset(imgLeft, rectT), size = Size((rectL - imgLeft).coerceAtLeast(0f), rectH))
                        // Right scrim
                        drawRect(scrimColor, topLeft = Offset(rectR, rectT), size = Size((imgLeft + imgDrawW - rectR).coerceAtLeast(0f), rectH))

                        // Rule of thirds lines
                        val gridColor = Color.White.copy(alpha = 0.35f)
                        drawLine(gridColor, Offset(rectL + rectW / 3f, rectT), Offset(rectL + rectW / 3f, rectB), strokeWidth = 1.5f)
                        drawLine(gridColor, Offset(rectL + 2f * rectW / 3f, rectT), Offset(rectL + 2f * rectW / 3f, rectB), strokeWidth = 1.5f)
                        drawLine(gridColor, Offset(rectL, rectT + rectH / 3f), Offset(rectR, rectT + rectH / 3f), strokeWidth = 1.5f)
                        drawLine(gridColor, Offset(rectL, rectT + 2f * rectH / 3f), Offset(rectR, rectT + 2f * rectH / 3f), strokeWidth = 1.5f)

                        // Crop border
                        drawRect(
                            color = CyanAccent,
                            topLeft = Offset(rectL, rectT),
                            size = Size(rectW, rectH),
                            style = Stroke(width = 3f)
                        )

                        // Corner handles (L-brackets + circles)
                        val handleRadius = 12f
                        val bracketLen = 28f
                        val strokeW = 6f
                        val corners = listOf(
                            Offset(rectL, rectT) to (1f to 1f),
                            Offset(rectR, rectT) to (-1f to 1f),
                            Offset(rectL, rectB) to (1f to -1f),
                            Offset(rectR, rectB) to (-1f to -1f)
                        )
                        corners.forEach { (pt, dir) ->
                            drawLine(CyanBright, pt, Offset(pt.x + dir.first * bracketLen, pt.y), strokeWidth = strokeW)
                            drawLine(CyanBright, pt, Offset(pt.x, pt.y + dir.second * bracketLen), strokeWidth = strokeW)
                            drawCircle(CyanAccent, radius = handleRadius, center = pt)
                            drawCircle(Color.White, radius = handleRadius * 0.45f, center = pt)
                        }
                    }
                }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Aspect Ratio Selector Tray (all 11 ratios visible & scrollable above system nav bar)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Navy800.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Navy700)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 156.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CropAspectRatio.values().forEach { ratio ->
                            val isSelected = selectedRatio == ratio
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyanAccent else Navy800)
                                    .border(
                                        1.dp,
                                        if (isSelected) CyanBright else Navy600,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedRatio = ratio
                                        cropRect = CropUtils.computeCenteredCropRect(
                                            imageWidth = sourceBitmap.width,
                                            imageHeight = sourceBitmap.height,
                                            aspectRatio = ratio
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("dialog_crop_ratio_${ratio.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ratio.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Navy900 else SlateTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
