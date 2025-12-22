package com.task.camera.recorder.presentation

import androidx.camera.core.SurfaceRequest

data class CameraSlice(
    val isRecording: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val surfaceRequest: SurfaceRequest? = null
)
