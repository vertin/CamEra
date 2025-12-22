package com.task.camera.details.presentation

sealed interface DetailsAction {
    data object NavigateBack : DetailsAction

    data class ShowSnackbar(val message: String) : DetailsAction
}
