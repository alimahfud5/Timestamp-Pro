package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SavedPhotoRepository
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.HistoryViewModel

enum class MainDestination(val label: String, val icon: ImageVector) {
    EDITOR("Editor", Icons.Default.Edit),
    TEMPLATES("Template", Icons.Default.Style),
    SETTINGS("Pengaturan", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    editorViewModel: EditorViewModel,
    historyViewModel: HistoryViewModel,
    repository: SavedPhotoRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(MainDestination.EDITOR) }
    var showHistoryScreen by rememberSaveable { mutableStateOf(false) }
    var savedQuality by rememberSaveable { mutableIntStateOf(95) }
    var preserveExif by rememberSaveable { mutableStateOf(true) }
    val savedPhotos by historyViewModel.savedPhotos.collectAsStateWithLifecycle()

    // Handle system back navigation cleanly
    BackHandler(enabled = showHistoryScreen || currentDestination != MainDestination.EDITOR) {
        if (showHistoryScreen) {
            showHistoryScreen = false
        } else {
            currentDestination = MainDestination.EDITOR
        }
    }

    if (showHistoryScreen) {
        HistoryScreen(
            viewModel = historyViewModel,
            onCameraPhotoCaptured = { uri ->
                editorViewModel.onPhotoSelected(context, uri)
                showHistoryScreen = false
                currentDestination = MainDestination.EDITOR
            },
            onBackClick = { showHistoryScreen = false },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Navy900,
        bottomBar = {
            NavigationBar(
                containerColor = Navy800,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Navy700)
            ) {
                MainDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = CyanBright,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = SlateTextSecondary,
                            unselectedTextColor = SlateTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (currentDestination) {
                MainDestination.EDITOR -> {
                    EditorScreen(
                        viewModel = editorViewModel,
                        repository = repository,
                        savedQuality = savedQuality,
                        preserveExif = preserveExif,
                        savedPhotoCount = savedPhotos.size,
                        onOpenHistoryClick = { showHistoryScreen = true }
                    )
                }
                MainDestination.TEMPLATES -> {
                    TemplatesScreen(
                        onSelectTemplate = { preset ->
                            editorViewModel.applyPreset(preset)
                            currentDestination = MainDestination.EDITOR
                        }
                    )
                }
                MainDestination.SETTINGS -> {
                    SettingsScreen(
                        savedQuality = savedQuality,
                        onQualityChange = { savedQuality = it },
                        preserveExif = preserveExif,
                        onPreserveExifChange = { preserveExif = it },
                        savedPhotoCount = savedPhotos.size,
                        onOpenHistoryClick = { showHistoryScreen = true }
                    )
                }
            }
        }
    }
}
