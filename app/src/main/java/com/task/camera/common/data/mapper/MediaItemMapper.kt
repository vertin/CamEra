package com.task.camera.common.data.mapper

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.Metadata
import com.task.camera.details.presentation.VideoFile

class MediaItemMapper {
    fun map(video: ExerciseVideo): VideoFile {
        val uri = video.filePath.toUri()

        return VideoFile(
            mediaItem = MediaItem.fromUri(uri),
            fileName = video.fileName,
            metadata = Metadata(durationMs = video.durationMs),
            status = video.status
        )
    }
}
