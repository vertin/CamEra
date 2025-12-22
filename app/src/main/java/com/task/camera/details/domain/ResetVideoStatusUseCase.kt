package com.task.camera.details.domain

import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository

class ResetVideoStatusUseCase(private val videoRepository: ExerciseVideoRepository) {
    suspend fun launch(videoId: Long): Result<Unit> {
        return videoRepository.updateVideoStatus(videoId, VideoStatus.RECORDED)
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { exception ->
                    Result.failure(
                        ResetVideoStatusException(
                            "Failed to reset video status for videoId: $videoId",
                            exception
                        )
                    )
                }
            )
    }
}

class ResetVideoStatusException(
    message: String,
    cause: Throwable
) : Exception(message, cause)
