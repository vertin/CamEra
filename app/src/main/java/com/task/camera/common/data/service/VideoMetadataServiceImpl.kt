package com.task.camera.common.data.service

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.net.toUri
import com.task.camera.common.domain.model.Metadata
import com.task.camera.common.domain.service.VideoMetadataService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VideoMetadataServiceImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : VideoMetadataService {

    override suspend fun extractMetadata(filePath: String): Result<Metadata> {
        return withContext(ioDispatcher) {
            val retriever = MediaMetadataRetriever()
            try {
                val uri = runCatching { filePath.toUri() }.getOrNull()
                val hasValidScheme = uri?.scheme != null

                if (hasValidScheme) {
                    retriever.setDataSource(context, uri)
                } else {
                    retriever.setDataSource(filePath)
                }

                val durationString =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationString?.toLongOrNull() ?: 0L

                Result.success(Metadata(durationMs = durationMs))
            } catch (e: OutOfMemoryError) {
                Log.e("VideoMetadataService", "Out of memory error: $filePath", e)
                Result.failure(e)
            } catch (e: IllegalArgumentException) {
                Log.d("VideoMetadataService", "Invalid URI format: $filePath", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("VideoMetadataService", "Failed to extract metadata from: $filePath", e)
                Result.failure(e)
            } finally {
                runCatching {
                    retriever.release()
                }
            }
        }
    }
}
