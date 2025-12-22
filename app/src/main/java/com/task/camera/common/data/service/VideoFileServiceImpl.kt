package com.task.camera.common.data.service

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.task.camera.common.domain.service.VideoFileService
import java.io.File

class VideoFileServiceImpl(
    private val context: Context
) : VideoFileService {

    override fun deleteFile(filePath: String) {
        val uri = runCatching { filePath.toUri() }.getOrNull()

        if (uri != null && uri.scheme != null) {
            try {
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted == 0) {
                    deleteAsFile(filePath)
                }
            } catch (e: Exception) {
                Log.w(
                    "VideoFileService",
                    "Failed to delete via content resolver, trying as file: $filePath",
                    e
                )
                deleteAsFile(filePath)
            }
        } else {
            deleteAsFile(filePath)
        }
    }

    override fun fileExists(filePath: String): Boolean {
        val uri = runCatching { filePath.toUri() }.getOrNull()

        return if (uri != null && uri.scheme != null) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    true
                } ?: false
            } catch (e: Exception) {
                Log.d(
                    "VideoFileService",
                    "File doesn't exist or can't access: $filePath",
                    e
                )
                false
            }
        } else {
            checkAsFile(filePath)
        }
    }

    private fun deleteAsFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            if (!file.delete()) {
                Log.e("VideoFileService", "Failed to delete video file: $filePath")
            }
        } else {
            Log.d("VideoFileService", "Video file doesn't exist (already deleted?): $filePath")
        }
    }

    private fun checkAsFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.isFile
    }
}
