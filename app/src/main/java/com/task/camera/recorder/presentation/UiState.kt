package com.task.camera.recorder.presentation

sealed interface UiState {
    data object Initialization : UiState
    data object NeedPermissions : UiState
    data class RecorderUiState(
        val cameraSlice: CameraSlice? = null,
    ) : UiState
}
