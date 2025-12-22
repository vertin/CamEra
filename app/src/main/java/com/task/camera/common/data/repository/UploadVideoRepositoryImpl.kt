package com.task.camera.common.data.repository

import com.task.camera.common.domain.repository.UploadVideoRepository
import kotlinx.coroutines.delay

class UploadVideoRepositoryImpl : UploadVideoRepository {

    override suspend fun uploadVideo(): Result<Unit> {
        return runCatching {
            delay(UPLOAD_SIMULATION_DELAY_MS)
        }
    }

    companion object {
        private const val UPLOAD_SIMULATION_DELAY_MS = 4000L
    }
}
