package com.task.camera.details.presentation.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SurfaceType

data class ContentFrameConfig(
    val surfaceType: @SurfaceType Int = SURFACE_TYPE_SURFACE_VIEW,
    val contentScale: ContentScale = ContentScale.FillBounds,
    val keepContentOnReset: Boolean = false,
    val shutter: @Composable () -> Unit = { Box(Modifier.fillMaxSize()) }
)
