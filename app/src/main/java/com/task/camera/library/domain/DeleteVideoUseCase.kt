package com.task.camera.library.domain

import android.util.Log
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoFileService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class DeleteVideoUseCase(
    private val repository: ExerciseVideoRepository,
    private val videoFileService: VideoFileService
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun launch(videoId: Long): Flow<Result<Unit>> {
        return repository.getVideoById(videoId)
            .flatMapLatest { videoResult ->
                flow {
                    videoResult.fold(
                        onSuccess = { video ->
                            runCatching {
                                videoFileService.deleteFile(video.filePath)
                            }.onFailure { fileException ->
                                Log.w(
                                    "DeleteVideoUseCase",
                                    "Failed to delete video file (file might not exist): ${video.filePath}",
                                    fileException
                                )
                            }

                            emit(repository.deleteVideo(videoId))
                        },
                        onFailure = { exception ->
                            emit(Result.failure(exception))
                        }
                    )
                }
            }
    }
}
