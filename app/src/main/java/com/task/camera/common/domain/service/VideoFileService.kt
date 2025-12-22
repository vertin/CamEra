package com.task.camera.common.domain.service

interface VideoFileService {
    fun deleteFile(filePath: String)
    fun fileExists(filePath: String): Boolean
}
