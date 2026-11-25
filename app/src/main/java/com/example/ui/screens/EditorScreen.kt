package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.data.local.SavedPhotoRepository
import com.example.data.model.CropAspectRatio
import com.example.ui.components.CropSection
import com.example.ui.components.LocationSection
import com.example.ui.components.LogoSection
import com.example.ui.components.StampPreview
import com.example.ui.components.StyleSection
import com.example.ui.components.TextSection
import com.example.ui.components.TimeSection
import com.example.ui.components.rememberCameraCaptureAction
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.EditorViewModel
import com.example.util.GalleryHelper
import com.example.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    repository: SavedPhotoRepository,
    savedQuality: Int = 95,
    preserveExif: Boolean = true,
    savedPhotoCount: Int = 0,
    onOpenHistoryClick: () -> Unit = {},
    onOpenCropClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPhotoSelected(context, uri)
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onLogoSelected(context, uri)
        }
    }

    val launchCamera = rememberCameraCaptureAction(
        onPhotoCaptured = { uri ->
            viewModel.onPhotoSelected(context, uri)
        },
        onPermissionDenied = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Izin kamera diperlukan untuk mengambil foto langsung dari kamera HP.")
            }
        },
        onLaunchError = { errorMsg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMsg)
            }
        }
    )

    var hasRequestedStartupLocation by rememberSaveable { mutableStateOf(false) }

    val startupLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchCurrentGps(context, silent = true)
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            viewModel.autoFetchDefaultGpsIfPermitted(context)
        } else if (!hasRequestedStartupLocation) {
            hasRequestedStartupLocation = true
            startupLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(uiState.config.logoUri) {
        viewModel.syncLogoBitmap(context)
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Navy900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900,
                    titleContentColor = SlateTextPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIMESTAMP PHOTO PRO",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = SlateTextPrimary
                            )
                        )
                    }
                },
                actions = {
                    // History Screen Shortcut (keeps History 1-tap accessible after removing bottom tab)
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = onOpenHistoryClick,
                            modifier = Modifier.testTag("action_open_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Riwayat Foto",
                                tint = CyanBright
                            )
                        }
                        if (savedPhotoCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp, end = 6.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent)
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (savedPhotoCount > 99) "99+" else savedPhotoCount.toString(),
                                    color = Navy900,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Primary Save Button
                    Button(
                        onClick = {
                            viewModel.savePhoto(
                                context = context,
                                repository = repository,
                                quality = savedQuality,
                                preserveExif = preserveExif
                            )
                        },
                        enabled = uiState.previewBitmap != null && !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Navy900,
                            disabledContainerColor = Navy800,
                            disabledContentColor = SlateTextSecondary.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_save_photo")
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Navy900,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 180.dp)
        ) {
            // 1. Photo Preview Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    StampPreview(
                        previewBitmap = uiState.previewBitmap,
                        config = uiState.config,
                        logoBitmap = uiState.logoBitmap,
                        onPickPhotoClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onOpenCameraClick = launchCamera,
                        onCropPhotoClick = {
                            if (uiState.sourcePreviewBitmap != null) {
                                onOpenCropClick()
                            }
                        }
                    )
                }
            }

            // 2. Quick Utility Toolbar + Editor Control Tabs
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickToolbarChip(
                            icon = Icons.Default.Undo,
                            label = "Undo",
                            enabled = uiState.canUndo,
                            tint = if (uiState.canUndo) CyanBright else SlateTextSecondary.copy(alpha = 0.4f),
                            onClick = { viewModel.undo() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_undo")
                        )
                        QuickToolbarChip(
                            icon = Icons.Default.RestartAlt,
                            label = "Reset",
                            enabled = true,
                            tint = SlateTextPrimary,
                            onClick = { viewModel.resetConfig(context) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_reset")
                        )
                        QuickToolbarChip(
                            icon = Icons.Default.History,
                            label = if (savedPhotoCount > 0) "Riwayat ($savedPhotoCount)" else "Riwayat",
                            enabled = true,
                            tint = CyanBright,
                            onClick = onOpenHistoryClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_quick_history")
                        )
                    }

                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Navy900,
                        contentColor = CyanAccent,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTab),
                                color = CyanAccent,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .border(1.dp, Navy800, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        val tabs = listOf(
                            Triple(0, Icons.Default.Schedule, "Waktu"),
                            Triple(1, Icons.Default.TextFields, "Teks"),
                            Triple(2, Icons.Default.FormatPaint, "Gaya"),
                            Triple(3, Icons.Default.LocationOn, "Lokasi"),
                            Triple(4, Icons.Default.Crop, "Crop"),
                            Triple(5, Icons.Default.Image, "Logo")
                        )
                        tabs.forEach { (index, icon, label) ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                selectedContentColor = CyanAccent,
                                unselectedContentColor = SlateTextSecondary,
                                modifier = Modifier.testTag("editor_tab_${label.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Tab Content
                    when (selectedTab) {
                        0 -> TimeSection(
                            config = uiState.config,
                            onConfigChange = { viewModel.updateConfig(it) }
                        )
                        1 -> TextSection(
                            config = uiState.config,
                            onConfigChange = { viewModel.updateConfig(it) }
                        )
                        2 -> StyleSection(
                            config = uiState.config,
                            onConfigChange = { viewModel.updateConfig(it) }
                        )
                        3 -> LocationSection(
                            config = uiState.config,
                            isGpsLoading = uiState.isGpsLoading,
                            onFetchGpsClick = { viewModel.fetchCurrentGps(context) },
                            onConfigChange = { viewModel.updateConfig(it) }
                        )
                        4 -> CropSection(
                            hasPhoto = uiState.sourcePreviewBitmap != null,
                            sourceWidth = uiState.originalWidth,
                            sourceHeight = uiState.originalHeight,
                            currentWidth = uiState.previewBitmap?.width ?: uiState.originalWidth,
                            currentHeight = uiState.previewBitmap?.height ?: uiState.originalHeight,
                            selectedAspectRatio = uiState.cropAspectRatio,
                            onSelectAspectRatio = { ratio ->
                                viewModel.applyCropAspectRatio(ratio)
                                if (ratio == CropAspectRatio.FREE && uiState.sourcePreviewBitmap != null) {
                                    onOpenCropClick()
                                }
                            },
                            onOpenInteractiveCrop = {
                                if (uiState.sourcePreviewBitmap != null) {
                                    onOpenCropClick()
                                }
                            },
                            onResetCrop = { viewModel.resetCrop() }
                        )
                        5 -> LogoSection(
                            config = uiState.config,
                            logoBitmap = uiState.logoBitmap,
                            onPickLogoClick = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveLogoClick = { viewModel.removeLogo() },
                            onRestoreDefaultLogoClick = { viewModel.restoreDefaultLogo(context) },
                            onConfigChange = { viewModel.updateConfig(it) }
                        )
                    }
                }
            }
        }

        // Save Result Dialog
        uiState.saveResult?.let { result ->
            if (result.success && result.savedUri != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSaveResult() },
                    containerColor = Navy800,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Foto Berhasil Disimpan!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Foto baru dengan stempel kualitas tinggi telah disimpan ke Galeri perangkat Anda (Pictures/TimestampPhotoPro) tanpa mengubah foto asli.",
                                style = MaterialTheme.typography.bodySmall.copy(color = SlateTextSecondary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Waktu: ${result.entity?.formattedTimestamp ?: "-"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = CyanBright)
                            )
                        }
                    },
                    confirmButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    GalleryHelper.openImageInGallery(context, result.savedUri)
                                    viewModel.dismissSaveResult()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CyanBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galeri", color = CyanBright, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, result.savedUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Foto Bertanda"))
                                    viewModel.dismissSaveResult()
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
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { viewModel.dismissSaveResult() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tutup", color = SlateTextPrimary)
                        }
                    }
                )
            } else if (!result.success) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSaveResult() },
                    containerColor = Navy800,
                    title = { Text("Gagal Menyimpan", color = SlateTextPrimary) },
                    text = { Text(result.errorMessage ?: "Terjadi kesalahan tidak diketahui", color = SlateTextSecondary) },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissSaveResult() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickToolbarChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = Navy800,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Navy700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = tint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}
