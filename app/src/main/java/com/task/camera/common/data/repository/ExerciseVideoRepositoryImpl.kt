package com.task.camera.common.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.task.camera.common.data.local.ExerciseVideoDao
import com.task.camera.common.data.mapper.toDomain
import com.task.camera.common.data.mapper.toEntity
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ExerciseVideoRepositoryImpl(
    private val dao: ExerciseVideoDao,
) : ExerciseVideoRepository {

    override fun getAllVideosPaged(): Flow<PagingData<ExerciseVideo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = {
                dao.getAllVideosPaged()
            }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    override fun getAllVideos(): Flow<Result<List<ExerciseVideo>>> {
        return dao.getAllVideos()
            .map { entities ->
                Result.success(entities.map { it.toDomain() })
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    override fun getVideoById(id: Long): Flow<Result<ExerciseVideo>> {
        return dao.getVideoById(id)
            .map { entity ->
                entity?.toDomain()?.let { Result.success(it) }
                    ?: Result.failure(NoSuchElementException("Video with id $id not found"))
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }

    override suspend fun saveVideo(video: ExerciseVideo): Result<Long> {
        return runCatching {
            dao.insertVideo(video.toEntity())
        }
    }

    override suspend fun updateVideoStatus(id: Long, status: VideoStatus): Result<Unit> {
        return runCatching {
            dao.updateStatus(id, status)
        }
    }

    override suspend fun deleteVideo(id: Long): Result<Unit> {
        return runCatching {
            dao.deleteVideo(id)
        }
    }
}
