package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogoPosition
import com.example.data.model.LogoSizePreset
import com.example.data.model.StampConfig
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Navy600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogoSection(
    config: StampConfig,
    logoBitmap: Bitmap?,
    onPickLogoClick: () -> Unit,
    onRemoveLogoClick: () -> Unit,
    onRestoreDefaultLogoClick: () -> Unit = {},
    onConfigChange: (StampConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Logo Picker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Navy900)
                            .border(1.dp, Navy600, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoBitmap != null && !logoBitmap.isRecycled) {
                            val imageBitmap = remember(logoBitmap) { logoBitmap.asImageBitmap() }
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Pratinjau Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(4.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Belum ada logo",
                                tint = SlateTextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Logo Foto",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val isDefaultLogo = config.logoUri == StampConfig.DEFAULT_LOGO_URI
                        Text(
                            text = when {
                                logoBitmap != null && isDefaultLogo ->
                                    "Logo default aktif (transparan) • Posisi: ${config.logoPosition.displayName} (${(config.logoSizePercent * 100).roundToInt()}%)"
                                logoBitmap != null ->
                                    "Logo kustom aktif • Posisi: ${config.logoPosition.displayName} (${(config.logoSizePercent * 100).roundToInt()}%)"
                                else ->
                                    "Tidak ada logo aktif. Gunakan logo default atau pilih PNG transparan dari galeri."
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
                        onClick = onPickLogoClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_pick_logo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (logoBitmap != null) "Ganti Logo" else "Tambah Logo",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (logoBitmap != null) {
                        OutlinedButton(
                            onClick = onRemoveLogoClick,
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_remove_logo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Logo",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hapus",
                                color = DangerRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (config.logoUri != StampConfig.DEFAULT_LOGO_URI) {
                    OutlinedButton(
                        onClick = onRestoreDefaultLogoClick,
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_restore_default_logo")
                    ) {
                        Text(
                            text = "Pakai Logo Default",
                            color = CyanBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 2. Position Selector (Default: Kanan Atas)
        Text(
            text = "POSISI LOGO (DEFAULT: KANAN ATAS)",
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
            LogoPosition.values().forEach { pos ->
                val isSelected = config.logoPosition == pos
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(logoPosition = pos)) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("logo_pos_${pos.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pos.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }

        // 3. Logo Size Controls (Presets + Fine Slider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UKURAN LOGO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "${(config.logoSizePercent * 100).roundToInt()}% lebar foto",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = CyanBright,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogoSizePreset.values().forEach { preset ->
                val isSelected = abs(config.logoSizePercent - preset.widthFraction) < 0.015f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(logoSizePercent = preset.widthFraction)) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("logo_size_${preset.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${preset.displayName} (${(preset.widthFraction * 100).roundToInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }

        Slider(
            value = config.logoSizePercent.coerceIn(0.06f, 0.35f),
            onValueChange = { onConfigChange(config.copy(logoSizePercent = it)) },
            valueRange = 0.06f..0.35f,
            colors = SliderDefaults.colors(
                thumbColor = CyanAccent,
                activeTrackColor = CyanAccent,
                inactiveTrackColor = Navy600
            ),
            modifier = Modifier.testTag("slider_logo_size")
        )
    }
}
