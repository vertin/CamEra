package com.task.camera.recorder.presentation

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.camera.recorder.camera.CameraManager
import com.task.camera.recorder.presentation.domain.SaveVideoFileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecorderViewModel(
    private val saveVideoFileUseCase: SaveVideoFileUseCase,
    private val cameraManager: CameraManager,
) : ViewModel() {
    private val _permissionSlice = MutableStateFlow<PermissionSlice>(PermissionSlice.OnRequest)

    val uiState = combine(
        _permissionSlice,
        cameraManager.cameraSlice
    ) { permissionSlice, cameraSlice ->
        if (permissionSlice is PermissionSlice.OnRequest || permissionSlice is PermissionSlice.Denied) {
            UiState.NeedPermissions
        } else {
            UiState.RecorderUiState(
                cameraSlice = cameraSlice,
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Initialization
        )

    private val _action = MutableSharedFlow<RecorderAction>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val action = _action.asSharedFlow()

    fun bind(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraManager.bindToLifecycle(lifecycleOwner)
        }
    }

    fun startRecording() {
        cameraManager.startVideoRecording()
    }

    fun stopRecording() {
        viewModelScope.launch {
            cameraManager.stopVideoRecording()?.let { videoResult ->
                saveVideoFileUseCase.launch(videoResult.filePath, videoResult.fileName)
                    .fold(
                        onSuccess = {
                            _action.emit(RecorderAction.NavigateBack)
                        },
                        onFailure = { exception ->
                            _action.emit(
                                RecorderAction.ShowError(
                                    exception.message ?: "Failed to save video"
                                )
                            )
                        }
                    )
            } ?: run {
                _action.emit(RecorderAction.ShowError("Failed to stop recording"))
            }
        }
    }

    fun permissionGranted() {
        _permissionSlice.update {
            PermissionSlice.Granted
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 500L
    }
}
