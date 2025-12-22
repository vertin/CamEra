package com.task.camera.details.presentation

import androidx.media3.common.MediaItem
import com.task.camera.common.domain.model.Metadata
import com.task.camera.common.domain.model.VideoStatus

data class VideoFile(
    val mediaItem: MediaItem,
    val fileName: String,
    val metadata: Metadata,
    val status: VideoStatus
)
