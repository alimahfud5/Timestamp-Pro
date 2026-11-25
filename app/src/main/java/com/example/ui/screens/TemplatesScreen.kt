package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.StampYellow

data class TemplateItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val config: StampConfig,
    val badgeLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onSelectTemplate: (StampConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates = listOf(
        TemplateItem(
            title = "Timemark Bersih (Jam Besar | Tanggal)",
            description = "Jam besar bergaya Oswald dengan garis vertikal emas, tanggal & hari bertumpuk di sebelahnya, serta alamat putih polos di kiri bawah tanpa watermark.",
            icon = Icons.Default.Verified,
            config = StampConfig.PRESET_TIMEMARK_AKURAT,
            badgeLabel = "BERSIH TANPA WATERMARK"
        ),
        TemplateItem(
            title = "Patroli Polsek Semarang Selatan",
            description = "Format dinas: 9 Nov 2026 09.45.17 WIB, 7 Jalan Pandanaran 2, Mugassari, Kec. Semarang Selatan, Jawa Tengah dengan koordinat GPS satelit dan badge kepolisian.",
            icon = Icons.Default.LocalPolice,
            config = StampConfig.PRESET_SEMARANG_SELATAN,
            badgeLabel = "POLSEK SEMARANG SELATAN"
        ),
        TemplateItem(
            title = "Inspeksi Proyek & Konstruksi",
            description = "Warna kuning safety dengan kontras tinggi, stempel kanan bawah, nama progres pekerjaan, pengawas lapangan QC, dan koordinat.",
            icon = Icons.Default.Construction,
            config = StampConfig.PRESET_PROYEK,
            badgeLabel = "TEKNIK & LAPANGAN"
        ),
        TemplateItem(
            title = "Dokumentasi Berita Acara & Resmi",
            description = "Mengutamakan waktu asli foto (EXIF), format ISO bersih, warna putih dengan kotak membulat rapi di kiri atas.",
            icon = Icons.Default.Assignment,
            config = StampConfig.PRESET_DOKUMENTASI_RESMI,
            badgeLabel = "AUDIT & LEGAL"
        ),
        TemplateItem(
            title = "Gaya Timemark (Jam Besar | Tanggal)",
            description = "Font angka tinggi Oswald dengan garis vertikal emas, tanggal & hari bertumpuk di sebelahnya, serta teks alamat putih polos tanpa kotak latar.",
            icon = Icons.Default.Schedule,
            config = StampConfig.PRESET_TIMEMARK,
            badgeLabel = "GAYA BARU"
        ),
        TemplateItem(
            title = "Pribadi & Travel Minimalis",
            description = "Tampilan simpel elegan tanpa kotak latar belakang, teks putih dengan bayangan tajam, cocok untuk foto liburan atau arsip pribadi.",
            icon = Icons.Default.TravelExplore,
            config = StampConfig.PRESET_MINIMALIS,
            badgeLabel = "MINIMALIS"
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900,
                    titleContentColor = SlateTextPrimary
                ),
                title = {
                    Text(
                        text = "TEMPLATE SIAP PAKAI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Pilih salah satu template di bawah untuk menerapkan konfigurasi stempel otomatis hanya dengan satu sentuhan.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                )
            }

            items(templates.size) { index ->
                val template = templates[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTemplate(template.config) }
                        .testTag("template_item_$index"),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Navy700)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Navy700)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = template.badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Navy700),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = template.icon,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = template.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SlateTextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onSelectTemplate(template.config) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Navy700,
                                contentColor = CyanBright
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Terapkan Template Ini", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
