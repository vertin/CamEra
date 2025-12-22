package com.task.camera.recorder.camera

import androidx.lifecycle.LifecycleOwner
import com.task.camera.recorder.presentation.CameraSlice
import kotlinx.coroutines.flow.StateFlow

interface CameraManager {
    val cameraSlice: StateFlow<CameraSlice?>

    suspend fun bindToLifecycle(lifecycleOwner: LifecycleOwner)
    fun startVideoRecording()
    suspend fun stopVideoRecording(): VideoRecordingResult?
}
