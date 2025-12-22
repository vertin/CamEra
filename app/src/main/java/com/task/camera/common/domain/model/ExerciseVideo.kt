package com.task.camera.common.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class ExerciseVideo(
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val durationMs: Long,
    val createdAt: LocalDateTime,
    val status: VideoStatus
)

enum class VideoStatus {
    RECORDED,
    UPLOADING,
    UPLOADED
}
