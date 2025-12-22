package com.task.camera.details.domain

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository

class SubmitVideoToUploadUseCase(
    private val videoRepository: ExerciseVideoRepository,
    private val workManager: WorkManager
) {
    suspend fun launch(videoId: Long): Result<Unit> {
        return videoRepository.updateVideoStatus(videoId, VideoStatus.UPLOADING).fold(
            onSuccess = {
                startWorker(videoId)
                Result.success(Unit)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    private fun startWorker(videoId: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val inputData = workDataOf(VideoUploadWorker.VIDEO_ID to videoId)
        val uploadWorkRequest = OneTimeWorkRequestBuilder<VideoUploadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        workManager.enqueue(uploadWorkRequest)
    }
}
