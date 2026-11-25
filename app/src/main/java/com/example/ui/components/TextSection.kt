package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.model.StampConfig
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@Composable
fun TextSection(
    config: StampConfig,
    onConfigChange: (StampConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle: Ikon Simbol Baris (📍 🌐 📋)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Navy800)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Ikon Simbol Baris (📍 🌐 📋)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )
                )
                Text(
                    text = "Hilangkan atau tampilkan ikon pada tiap baris info",
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
                modifier = Modifier.testTag("text_switch_line_icons")
            )
        }

        // Section: Nama Kegiatan
        MetadataFieldCard(
            title = "Nama Kegiatan / Acara",
            isEnabled = config.includeActivity,
            onToggle = { onConfigChange(config.copy(includeActivity = it)) },
            content = {
                OutlinedTextField(
                    value = config.activityName,
                    onValueChange = { onConfigChange(config.copy(activityName = it)) },
                    placeholder = { Text("Contoh: Patroli Kamtibmas / Inspeksi Lapangan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_activity_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Navy600,
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Patroli Siang", "Inspeksi", "Dokumentasi Giat").forEach { sample ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Navy700)
                                .clickable { onConfigChange(config.copy(activityName = sample, includeActivity = true)) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ $sample",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanBright,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        )

        // Section: Nama Petugas
        MetadataFieldCard(
            title = "Nama Petugas / Pelapor",
            isEnabled = config.includeOfficer,
            onToggle = { onConfigChange(config.copy(includeOfficer = it)) },
            content = {
                OutlinedTextField(
                    value = config.officerName,
                    onValueChange = { onConfigChange(config.copy(officerName = it)) },
                    placeholder = { Text("Contoh: Bripka Santoso / Tim Lapangan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_officer_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Navy600,
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        )

        // Section: Keterangan / Catatan
        MetadataFieldCard(
            title = "Keterangan / Catatan Foto",
            isEnabled = config.includeNotes,
            onToggle = { onConfigChange(config.copy(includeNotes = it)) },
            content = {
                OutlinedTextField(
                    value = config.notes,
                    onValueChange = { onConfigChange(config.copy(notes = it)) },
                    placeholder = { Text("Contoh: Kondisi aman terkendali, cuaca cerah") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Navy600,
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MetadataFieldCard(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )
                )

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanAccent,
                        checkedTrackColor = Navy600,
                        uncheckedThumbColor = SlateTextSecondary,
                        uncheckedTrackColor = Navy900
                    )
                )
            }

            if (isEnabled) {
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                val coroutineScope = rememberCoroutineScope()
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.hasFocus) {
                                coroutineScope.launch {
                                    delay(250)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                ) {
                    content()
                }
            }
        }
    }
}
