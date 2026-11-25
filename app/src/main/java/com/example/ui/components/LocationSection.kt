package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationSection(
    config: StampConfig,
    isGpsLoading: Boolean,
    onFetchGpsClick: () -> Unit,
    onConfigChange: (StampConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            onFetchGpsClick()
        }
    }

    fun requestGps() {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
            onFetchGpsClick()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick GPS Acquisition Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Deteksi Koordinat Satelit",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                        )
                        Text(
                            text = "Ambil data GPS realtime perangkat untuk koordinat akurat",
                            style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { requestGps() },
                        enabled = !isGpsLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_fetch_gps")
                    ) {
                        if (isGpsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Navy900,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ambil GPS", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (config.latitude != null && config.longitude != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Navy900, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LAT: ${config.latitude} | LON: ${config.longitude}${config.altitude?.let { " | ALT: ${it.toInt()}m" } ?: ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanBright,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Toggle: Tampilkan Koordinat GPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cantumkan Koordinat GPS pada Foto",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextPrimary
                        )
                    )
                    Text(
                        text = "Menampilkan angka Latitude, Longitude & Elevasi",
                        style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                    )
                }

                Switch(
                    checked = config.includeGps,
                    onCheckedChange = { onConfigChange(config.copy(includeGps = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanAccent,
                        checkedTrackColor = Navy600,
                        uncheckedThumbColor = SlateTextSecondary,
                        uncheckedTrackColor = Navy900
                    )
                )
            }
        }

        // Toggle: Tampilkan Nama Lokasi / Alamat
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
                            text = "Cantumkan Nama Alamat / Tempat",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SlateTextPrimary
                            )
                        )
                        Text(
                            text = "Jalan, Kelurahan, Kecamatan, Provinsi",
                            style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                        )
                    }

                    Switch(
                        checked = config.includeLocation,
                        onCheckedChange = { onConfigChange(config.copy(includeLocation = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = Navy600,
                            uncheckedThumbColor = SlateTextSecondary,
                            uncheckedTrackColor = Navy900
                        )
                    )
                }

                if (config.includeLocation) {
                    val addressBringIntoView = remember { BringIntoViewRequester() }
                    val coroutineScope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(addressBringIntoView)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = config.locationName,
                            onValueChange = { onConfigChange(config.copy(locationName = it)) },
                            placeholder = { Text("Mengikuti GPS saat ini...\nAtau ketik manual: Jalan, Kelurahan, Kecamatan, Provinsi") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusEvent { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            delay(250)
                                            addressBringIntoView.bringIntoView()
                                        }
                                    }
                                }
                                .testTag("input_location_name"),
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick chip for Gg XIV Lamper Tengah, Semarang Selatan, Jawa Tengah
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy700)
                                .clickable {
                                    onConfigChange(
                                        config.copy(
                                            locationName = "Gg XIV\nLamper Tengah\nKecamatan Semarang Selatan\nJawa Tengah",
                                            latitude = config.latitude ?: -7.00180,
                                            longitude = config.longitude ?: 110.44080,
                                            altitude = config.altitude ?: 12.0,
                                            includeLocation = true,
                                            includeGps = true
                                        )
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("chip_quick_fill_lamper_tengah")
                        ) {
                            Text(
                                text = "📍 Isi Cepat: Gg XIV Lamper Tengah, Semarang Selatan, Jawa Tengah",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanBright,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
