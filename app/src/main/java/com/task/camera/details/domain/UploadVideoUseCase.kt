package com.task.camera.details.domain

import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.repository.UploadVideoRepository

class UploadVideoUseCase(
    private val uploadVideoRepository: UploadVideoRepository,
    private val exerciseVideoRepository: ExerciseVideoRepository
) {
    suspend fun launch(videoId: Long): Result<Unit> {
        return uploadVideoRepository.uploadVideo()
            .fold(
                onSuccess = {
                    exerciseVideoRepository.updateVideoStatus(videoId, VideoStatus.UPLOADED)
                        .fold(
                            onSuccess = { Result.success(Unit) },
                            onFailure = { exception ->
                                Result.failure(
                                    UploadVideoException(
                                        "Failed to update video status to UPLOADED for videoId: $videoId",
                                        exception
                                    )
                                )
                            }
                        )
                },
                onFailure = { exception ->
                    Result.failure(
                        UploadVideoException(
                            "Failed to upload video for videoId: $videoId",
                            exception
                        )
                    )
                }
            )
    }
}

class UploadVideoException(
    message: String,
    cause: Throwable
) : Exception(message, cause)
