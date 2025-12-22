package com.task.camera.common.domain.repository

interface UploadVideoRepository {
    suspend fun uploadVideo(): Result<Unit>
}
