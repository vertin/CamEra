package com.task.camera.library.presentation

sealed interface LibraryAction {
    data object RecordNewVideo : LibraryAction
    data class OpenDetails(val videoId: Long) : LibraryAction
    data class ShowDeleteError(val message: String) : LibraryAction
}
