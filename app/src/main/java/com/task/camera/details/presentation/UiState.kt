package com.task.camera.details.presentation

sealed interface UiState {

    data object Loading : UiState

    data class Error(val errorMessage: String) : UiState

    data class DetailsUiState(
        val videoFile: VideoFile
    ) : UiState
}
