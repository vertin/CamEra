package com.task.camera.details.domain

import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoFileService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class GetVideoByIdUseCase(
    private val repository: ExerciseVideoRepository,
    private val videoFileService: VideoFileService
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun launch(videoId: Long): Flow<Result<ExerciseVideo>> {
        return repository.getVideoById(videoId)
            .flatMapLatest { result ->
                flow {
                    result.fold(
                        onSuccess = { video ->
                            if (!videoFileService.fileExists(video.filePath)) {
                                repository.deleteVideo(videoId)
                                emit(Result.failure(IllegalStateException("Video file not found")))
                            } else {
                                emit(Result.success(video))
                            }
                        },
                        onFailure = { exception ->
                            emit(Result.failure(exception))
                        }
                    )
                }
            }
    }
}
