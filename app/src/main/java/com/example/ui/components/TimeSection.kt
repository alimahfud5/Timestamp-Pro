package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.StampConfig
import com.example.data.model.TimestampSource
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.SlateTextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun TimeSection(
    config: StampConfig,
    onConfigChange: (StampConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCustomFormatExpanded by remember { mutableStateOf(false) }

    val presetFormats = listOf(
        "d MMM yyyy HH.mm.ss 'WIB'",
        "dd/MM/yyyy HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "dd MMM yyyy, HH:mm",
        "EEEE, dd MMMM yyyy HH:mm:ss",
        "dd-MM-yyyy hh:mm:ss a"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Source Selector
        Text(
            text = "SUMBER TANGGAL & WAKTU",
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
            TimestampSource.values().forEach { source ->
                val isSelected = config.source == source
                val isExifAvailable = source != TimestampSource.ORIGINAL_EXIF || config.originalExifTimestamp != null

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyanAccent else Navy800)
                        .border(
                            1.dp,
                            if (isSelected) CyanBright else Navy600,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isExifAvailable) {
                            onConfigChange(config.copy(source = source))
                        }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("source_chip_${source.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (source) {
                                TimestampSource.CURRENT -> "Sekarang"
                                TimestampSource.ORIGINAL_EXIF -> "Asli EXIF"
                                TimestampSource.MANUAL -> "Manual"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Navy900 else if (isExifAvailable) SlateTextPrimary else SlateTextTertiary
                            )
                        )
                        if (source == TimestampSource.ORIGINAL_EXIF && config.originalExifTimestamp == null) {
                            Text(
                                text = "Tidak ada",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    color = SlateTextTertiary
                                )
                            )
                        }
                    }
                }
            }
        }

        // If manual time is chosen, show date & time picker buttons
        if (config.source == TimestampSource.MANUAL) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Waktu Manual Dipilih:",
                                style = MaterialTheme.typography.labelSmall.copy(color = SlateTextSecondary)
                            )
                            val manualDisplay = SimpleDateFormat("d MMM yyyy, HH.mm.ss", Locale("id", "ID"))
                                .format(Date(config.manualTimestamp))
                            Text(
                                text = manualDisplay,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SlateTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val calendar = Calendar.getInstance().apply { timeInMillis = config.manualTimestamp }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                calendar.set(Calendar.MINUTE, minute)
                                                calendar.set(Calendar.SECOND, 0)
                                                onConfigChange(config.copy(manualTimestamp = calendar.timeInMillis))
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubah")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset button for 9 Nov 2026 09.45.17 WIB
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Navy700)
                            .clickable {
                                val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Jakarta")).apply {
                                    set(2026, Calendar.NOVEMBER, 9, 9, 45, 17)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onConfigChange(
                                    config.copy(
                                        manualTimestamp = cal.timeInMillis,
                                        dateFormatPattern = "d MMM yyyy HH.mm.ss 'WIB'"
                                    )
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ Gunakan 9 Nov 2026 09.45.17 WIB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Format selector
        Text(
            text = "PILIHAN FORMAT TANGGAL & JAM",
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
            presetFormats.forEach { pattern ->
                val isSelected = config.dateFormatPattern == pattern
                val previewSample = try {
                    SimpleDateFormat(pattern, Locale("id", "ID")).format(Date(config.getResolvedTimestamp()))
                } catch (e: Exception) {
                    pattern
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onConfigChange(config.copy(dateFormatPattern = pattern)) },
                    label = {
                        Text(
                            text = previewSample,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = Navy900,
                        containerColor = Navy800,
                        labelColor = SlateTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Navy600,
                        selectedBorderColor = CyanBright
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Custom format input
        val formatBringIntoView = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()
        OutlinedTextField(
            value = config.dateFormatPattern,
            onValueChange = { onConfigChange(config.copy(dateFormatPattern = it)) },
            label = { Text("Pola Format Kustom (Java SimpleDateFormat)") },
            placeholder = { Text("Contoh: dd/MM/yyyy HH:mm:ss") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(formatBringIntoView)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            delay(250)
                            formatBringIntoView.bringIntoView()
                        }
                    }
                }
                .testTag("input_custom_date_format"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = Navy600,
                focusedLabelColor = CyanAccent,
                unfocusedLabelColor = SlateTextSecondary,
                focusedTextColor = SlateTextPrimary,
                unfocusedTextColor = SlateTextPrimary,
                focusedContainerColor = Navy800,
                unfocusedContainerColor = Navy800
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
