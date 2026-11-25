package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedPhotoEntity
import com.example.data.local.SavedPhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: SavedPhotoRepository) : ViewModel() {

    val savedPhotos: StateFlow<List<SavedPhotoEntity>> = repository.allPhotos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deletePhoto(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearAllPhotos() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
