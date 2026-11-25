package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.SavedPhotoEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.SlateTextTertiary
import com.example.ui.components.rememberCameraCaptureAction
import com.example.ui.viewmodel.HistoryViewModel
import com.example.util.GalleryHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onCameraPhotoCaptured: (Uri) -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photos by viewModel.savedPhotos.collectAsStateWithLifecycle()
    var selectedPhotoForPreview by remember { mutableStateOf<SavedPhotoEntity?>(null) }
    var photoToDelete by remember { mutableStateOf<SavedPhotoEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val launchCamera = rememberCameraCaptureAction(
        onPhotoCaptured = onCameraPhotoCaptured,
        onPermissionDenied = {
            android.widget.Toast.makeText(
                context,
                "Izin kamera diperlukan untuk mengambil foto langsung dari kamera HP.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        },
        onLaunchError = { errorMsg ->
            android.widget.Toast.makeText(
                context,
                errorMsg,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
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
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("history_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = SlateTextPrimary
                            )
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RIWAYAT FOTO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        if (photos.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Navy700)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${photos.size} foto",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CyanBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = launchCamera,
                        modifier = Modifier.testTag("history_open_camera")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Masuk Camera",
                            tint = CyanBright
                        )
                    }
                    if (photos.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Hapus Semua",
                                tint = SlateTextSecondary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (photos.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Navy800)
                            .border(1.dp, Navy700, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = CyanAccent.copy(alpha = 0.5f),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Foto Tersimpan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Foto yang Anda beri stempel tanggal, jam, dan lokasi akan muncul di riwayat ini dan tersimpan di Galeri.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SlateTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    HistoryPhotoCard(
                        photo = photo,
                        onPreviewClick = { selectedPhotoForPreview = photo },
                        onShareClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(photo.savedUri))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Foto Bertanda"))
                        },
                        onDeleteClick = { photoToDelete = photo }
                    )
                }
            }
        }

        // Full Screen Preview Dialog
        selectedPhotoForPreview?.let { photo ->
            Dialog(onDismissRequest = { selectedPhotoForPreview = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Navy900)
                        .border(1.dp, CyanAccent, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Preview Detail Foto",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                            )
                            IconButton(onClick = { selectedPhotoForPreview = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = SlateTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = photo.savedUri,
                                contentDescription = "Foto tersimpan",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Navy800, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "📅 " + photo.formattedTimestamp,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = CyanBright,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            photo.locationText?.let {
                                Text(
                                    text = "📍 $it",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextPrimary)
                                )
                            }
                            photo.gpsCoordsText?.let {
                                Text(
                                    text = "🌐 $it",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                                )
                            }
                            photo.activityName?.let {
                                Text(
                                    text = "📋 $it",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextPrimary)
                                )
                            }
                            photo.officerName?.let {
                                Text(
                                    text = "👮 $it",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    GalleryHelper.openImageInGallery(context, Uri.parse(photo.savedUri))
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Galeri", color = CyanBright, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, Uri.parse(photo.savedUri))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Foto Bertanda"))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Navy900
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirm Dialog
        photoToDelete?.let { photo ->
            AlertDialog(
                onDismissRequest = { photoToDelete = null },
                containerColor = Navy800,
                title = { Text("Hapus dari Riwayat?", color = SlateTextPrimary) },
                text = {
                    Text(
                        "Foto ini akan dihapus dari daftar riwayat aplikasi.",
                        color = SlateTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePhoto(photo.id)
                            photoToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { photoToDelete = null }) {
                        Text("Batal", color = SlateTextPrimary)
                    }
                }
            )
        }

        // Clear All Confirm Dialog
        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                containerColor = Navy800,
                title = { Text("Bersihkan Semua Riwayat?", color = SlateTextPrimary) },
                text = {
                    Text(
                        "Seluruh riwayat pembuatan stempel foto akan dikosongkan.",
                        color = SlateTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllPhotos()
                            showClearAllDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Bersihkan Semua", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearAllDialog = false }) {
                        Text("Batal", color = SlateTextPrimary)
                    }
                }
            )
        }
    }
}

@Composable
private fun HistoryPhotoCard(
    photo: SavedPhotoEntity,
    onPreviewClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreviewClick() }
            .testTag("history_card_${photo.id}"),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy900)
                    .border(1.dp, Navy700, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photo.savedUri,
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = photo.formattedTimestamp,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                )

                photo.locationText?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SlateTextPrimary
                        )
                    )
                }

                photo.activityName?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SlateTextSecondary
                        )
                    )
                }

                Text(
                    text = "${photo.width} × ${photo.height} px",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SlateTextTertiary,
                        fontSize = 10.sp
                    )
                )
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = DangerRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
