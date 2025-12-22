package com.task.camera.common.domain.repository

import androidx.paging.PagingData
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import kotlinx.coroutines.flow.Flow

interface ExerciseVideoRepository {
    fun getAllVideosPaged(): Flow<PagingData<ExerciseVideo>>
    fun getAllVideos(): Flow<Result<List<ExerciseVideo>>>
    fun getVideoById(id: Long): Flow<Result<ExerciseVideo>>
    suspend fun saveVideo(video: ExerciseVideo): Result<Long>
    suspend fun updateVideoStatus(id: Long, status: VideoStatus): Result<Unit>
    suspend fun deleteVideo(id: Long): Result<Unit>
}
