package com.task.camera.recorder.presentation

sealed interface RecorderAction {
    data object NavigateBack : RecorderAction
    data class ShowError(val message: String) : RecorderAction
}
