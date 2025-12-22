package com.task.camera.recorder.presentation

sealed interface PermissionSlice {
    data object OnRequest : PermissionSlice
    data object Granted : PermissionSlice
    data object Denied : PermissionSlice
}
