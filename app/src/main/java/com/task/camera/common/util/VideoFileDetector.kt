package com.task.camera.common.util

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File

/**
 * Video file detector - determines if a file is a video
 * Extracted to a separate class for improved testability
 */
class VideoFileDetector {
    fun isVideoFile(data: Any): Boolean {
        return when (data) {
            is String -> isVideoString(data)
            is Uri -> isVideoUri(data)
            is File -> isVideoFileExtension(data.path)
            else -> false
        }
    }

    private fun isVideoString(data: String): Boolean {
        val uri = try {
            data.toUri()
        } catch (e: IllegalArgumentException) {
            Log.d("VideoFileDetector", "Invalid URI format: $data", e)
            null
        }

        return if (uri != null && uri.scheme != null) {
            uri.scheme == CONTENT_SCHEME &&
                (
                    uri.path?.contains(VIDEO_PATH_INDICATOR, ignoreCase = true) == true ||
                        data.contains(VIDEO_PATH_INDICATOR, ignoreCase = true)
                    )
        } else {
            isVideoFileExtension(data)
        }
    }

    private fun isVideoUri(uri: Uri): Boolean {
        return uri.path?.contains(VIDEO_PATH_INDICATOR, ignoreCase = true) == true ||
            uri.scheme == CONTENT_SCHEME
    }

    fun isVideoFileExtension(path: String): Boolean {
        return path.endsWith(EXTENSION_MP4, ignoreCase = true) ||
            path.endsWith(EXTENSION_MKV, ignoreCase = true) ||
            path.endsWith(EXTENSION_WEBM, ignoreCase = true) ||
            path.endsWith(EXTENSION_3GP, ignoreCase = true)
    }

    companion object {
        private const val VIDEO_PATH_INDICATOR = "/video/"
        private const val CONTENT_SCHEME = "content"
        private const val EXTENSION_MP4 = ".mp4"
        private const val EXTENSION_MKV = ".mkv"
        private const val EXTENSION_WEBM = ".webm"
        private const val EXTENSION_3GP = ".3gp"
    }
}
