package com.task.camera.details.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class VideoUploadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val useCase: UploadVideoUseCase,
    private val resetStateUseCase: ResetVideoStatusUseCase
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val videoId = inputData.getLong(VIDEO_ID, INVALID_VIDEO_ID)

        if (videoId == INVALID_VIDEO_ID) return Result.failure()

        return useCase.launch(videoId).fold(
            onSuccess = {
                Result.success()
            },
            onFailure = { exception ->
                when {
                    runAttemptCount < MAX_ATTEMPT -> {
                        Result.retry()
                    }
                    else -> {
                        resetStateUseCase.launch(videoId)
                            .fold(
                                onSuccess = { },
                                onFailure = { resetException ->
                                    Log.e(
                                        "VideoUploadWorker",
                                        "Failed to reset video status for videoId: $videoId",
                                        resetException
                                    )
                                }
                            )
                        Log.e(
                            "VideoUploadWorker",
                            "Failed to upload video after $MAX_ATTEMPT attempts for videoId: $videoId",
                            exception
                        )
                        Result.failure()
                    }
                }
            }
        )
    }

    companion object {
        const val VIDEO_ID = "videoId"
        private const val MAX_ATTEMPT = 3
        private const val INVALID_VIDEO_ID = -1L
    }
}
