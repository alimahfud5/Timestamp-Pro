package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SavedPhotoRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.TimestampPhotoTheme
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.HistoryViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { SavedPhotoRepository(database.savedPhotoDao()) }

    private val editorViewModel: EditorViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimestampPhotoTheme {
                MainScreen(
                    editorViewModel = editorViewModel,
                    historyViewModel = historyViewModel,
                    repository = repository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
