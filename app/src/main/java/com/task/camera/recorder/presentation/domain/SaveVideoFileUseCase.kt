package com.task.camera.recorder.presentation.domain

import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoMetadataService
import java.time.LocalDateTime

class SaveVideoFileUseCase(
    private val repository: ExerciseVideoRepository,
    private val metadataService: VideoMetadataService
) {
    suspend fun launch(filePath: String, fileName: String): Result<Long> {
        return metadataService.extractMetadata(filePath)
            .fold(
                onSuccess = { metadata ->
                    val video = ExerciseVideo(
                        filePath = filePath,
                        fileName = fileName,
                        durationMs = metadata.durationMs,
                        createdAt = LocalDateTime.now(),
                        status = VideoStatus.RECORDED
                    )
                    repository.saveVideo(video)
                },
                onFailure = { exception ->
                    val video = ExerciseVideo(
                        filePath = filePath,
                        fileName = fileName,
                        durationMs = 0L,
                        createdAt = LocalDateTime.now(),
                        status = VideoStatus.RECORDED
                    )
                    repository.saveVideo(video)
                        .fold(
                            onSuccess = { id -> Result.success(id) },
                            onFailure = { saveException ->
                                Result.failure(
                                    SaveVideoFileException(
                                        "Failed to save video: $fileName. " +
                                            "Metadata extraction failed: ${exception.message}",
                                        saveException
                                    )
                                )
                            }
                        )
                }
            )
    }
}

class SaveVideoFileException(
    message: String,
    cause: Throwable
) : Exception(message, cause)
