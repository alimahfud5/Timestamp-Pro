package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StampBackgroundStyle
import com.example.data.model.StampConfig
import com.example.data.model.StampFontFamily
import com.example.data.model.StampFontSize
import com.example.data.model.StampPosition
import com.example.data.model.StampTextAlign
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleSection(
    config: StampConfig,
    onConfigChange: (StampConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPalette = listOf(
        0xFF00E5FF, // Cyan Pro
        0xFFFFFFFF, // Pure White
        0xFFFFEB3B, // Warning Yellow
        0xFF76FF03, // Lime Green
        0xFFFF9100, // Vibrant Orange
        0xFFFF5252, // Coral Red
        0xFF0A0F1D  // Deep Black
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Position Selector Grid
        Text(
            text = "POSISI STEMPEL",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StampPosition.values().forEach { pos ->
                val isSelected = config.position == pos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(position = pos)) }
                        .padding(vertical = 10.dp, horizontal = 2.dp)
                        .testTag("pos_${pos.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (pos) {
                            StampPosition.TOP_LEFT -> "Kiri Atas"
                            StampPosition.TOP_RIGHT -> "Kanan Atas"
                            StampPosition.BOTTOM_LEFT -> "Kiri Bawah"
                            StampPosition.BOTTOM_RIGHT -> "Kanan Bawah"
                            StampPosition.CENTER -> "Tengah"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // 1b. Text Alignment (Rata Kanan, Rata Kiri, Rata Tengah)
        Text(
            text = "PENATAAN TEKS (ALIGNMENT)",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StampTextAlign.values().forEach { align ->
                val isSelected = config.textAlign == align
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(textAlign = align)) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = align.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }

        // 2. Font Size Selector (Tiny, Small, Normal, Medium, Large, Extra Large)
        val selectedFontSize = StampFontSize.fromSp(config.fontSizeSp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UKURAN FONT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = selectedFontSize.displayName,
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
            StampFontSize.values().forEach { fontSize ->
                val isSelected = selectedFontSize == fontSize
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(fontSizeSp = fontSize.sizeSp)) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("font_size_${fontSize.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fontSize.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }

        // 3. Font Family Selector
        Text(
            text = "JENIS FONT",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        )

        val oswaldComposeFont = FontFamily(Font(R.font.oswald, FontWeight.Bold))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StampFontFamily.values().forEach { font ->
                val isSelected = config.fontFamily == font
                val chipFontFamily = when (font) {
                    StampFontFamily.MONOSPACE -> FontFamily.Monospace
                    StampFontFamily.SANS_SERIF -> FontFamily.SansSerif
                    StampFontFamily.SERIF -> FontFamily.Serif
                    StampFontFamily.BOLD_HEAVY -> FontFamily.Default
                    StampFontFamily.CONDENSED, StampFontFamily.TIMEMARK -> oswaldComposeFont
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(fontFamily = font)) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("font_${font.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (font) {
                            StampFontFamily.MONOSPACE -> "Digital"
                            StampFontFamily.SANS_SERIF -> "Sans"
                            StampFontFamily.SERIF -> "Serif"
                            StampFontFamily.BOLD_HEAVY -> "Tebal"
                            StampFontFamily.CONDENSED -> "Condensed"
                            StampFontFamily.TIMEMARK -> "Timemark (06:15 | Tgl)"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = chipFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }

        // 4. Color Palette Picker
        Text(
            text = "WARNA TEKS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            colorPalette.forEach { colorVal ->
                val isSelected = config.textColorHex == colorVal
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(colorVal))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Navy600,
                            shape = CircleShape
                        )
                        .clickable { onConfigChange(config.copy(textColorHex = colorVal)) }
                        .testTag("color_${colorVal}")
                )
            }
        }

        // 5. Background Style
        Text(
            text = "GAYA LATAR BELAKANG",
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
            StampBackgroundStyle.values().forEach { style ->
                val isSelected = config.backgroundStyle == style
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfigChange(config.copy(backgroundStyle = style)) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Navy900 else SlateTextPrimary
                        )
                    )
                }
            }
        }

        // 6. Background Opacity Slider (if not NONE)
        if (config.backgroundStyle != StampBackgroundStyle.NONE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transparansi Latar Belakang",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )
                )
                Text(
                    text = "${(config.backgroundOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CyanBright,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Slider(
                value = config.backgroundOpacity,
                onValueChange = { onConfigChange(config.copy(backgroundOpacity = it)) },
                valueRange = 0.2f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = Navy600
                ),
                modifier = Modifier.testTag("slider_bg_opacity")
            )
        }

        // 7. Shadow Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Navy800)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Bayangan Teks (Shadow)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )
                )
                Text(
                    text = "Meningkatkan keterbacaan teks di atas foto terang",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                )
            }

            Switch(
                checked = config.hasShadow,
                onCheckedChange = { onConfigChange(config.copy(hasShadow = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    checkedTrackColor = Navy600,
                    uncheckedThumbColor = SlateTextSecondary,
                    uncheckedTrackColor = Navy900
                )
            )
        }

        // 8. Show/Hide Line Icons Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Navy800)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Ikon Simbol Teks (📍 🌐 📋)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )
                )
                Text(
                    text = "Aktifkan untuk menambah ikon atau matikan untuk teks polos",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                )
            }

            Switch(
                checked = config.showLineIcons,
                onCheckedChange = { onConfigChange(config.copy(showLineIcons = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    checkedTrackColor = Navy600,
                    uncheckedThumbColor = SlateTextSecondary,
                    uncheckedTrackColor = Navy900
                ),
                modifier = Modifier.testTag("switch_line_icons")
            )
        }

        // 9. Timemark Verified Watermark Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Navy800)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Watermark Timemark (100% Akurat)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )
                )
                Text(
                    text = "Menampilkan label kanan atas & stempel vertikal Verified",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                )
            }

            Switch(
                checked = config.showTimemarkWatermark,
                onCheckedChange = { onConfigChange(config.copy(showTimemarkWatermark = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    checkedTrackColor = Navy600,
                    uncheckedThumbColor = SlateTextSecondary,
                    uncheckedTrackColor = Navy900
                ),
                modifier = Modifier.testTag("switch_timemark_watermark")
            )
        }
    }
}
