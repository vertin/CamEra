package com.task.camera.library.domain

import androidx.paging.PagingData
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import kotlinx.coroutines.flow.Flow

class GetAllVideosPagedUseCase(
    private val repository: ExerciseVideoRepository
) {
    fun launch(): Flow<PagingData<ExerciseVideo>> {
        return repository.getAllVideosPaged()
    }
}
