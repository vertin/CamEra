package com.task.camera.common.domain.service

import com.task.camera.common.domain.model.Metadata

interface VideoMetadataService {
    suspend fun extractMetadata(filePath: String): Result<Metadata>
}
