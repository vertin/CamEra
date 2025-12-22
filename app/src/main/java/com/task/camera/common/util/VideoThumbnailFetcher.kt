package com.task.camera.common.util

import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import java.io.File
import java.io.IOException

class VideoThumbnailFetcher(
    private val data: Any,
    private val options: Options,
    private val retrieverFactory: () -> MediaMetadataRetrieverWrapper = { MediaMetadataRetrieverWrapperImpl() }
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val retriever = retrieverFactory()
        return try {
            if (setDataSource(retriever) != true) {
                null
            } else {
                val bitmap = retriever.getFrameAtTime(
                    THUMBNAIL_TIME_MICROSECONDS,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )

                bitmap?.let {
                    DrawableResult(
                        drawable = BitmapDrawable(options.context.resources, it),
                        isSampled = false,
                        dataSource = DataSource.DISK
                    )
                }
            }
        } catch (e: IllegalStateException) {
            Log.e("VideoThumbnailFetcher", "MediaMetadataRetriever illegal state", e)
            null
        } catch (e: IOException) {
            Log.e("VideoThumbnailFetcher", "IO error reading video", e)
            null
        } catch (e: RuntimeException) {
            Log.e("VideoThumbnailFetcher", "Unexpected error fetching thumbnail", e)
            null
        } finally {
            retriever.release()
        }
    }

    private fun setDataSource(retriever: MediaMetadataRetrieverWrapper): Boolean? {
        return when (data) {
            is Uri -> {
                try {
                    retriever.setDataSource(options.context, data)
                    true
                } catch (e: IllegalStateException) {
                    Log.e("VideoThumbnailFetcher", "Failed to set data source from Uri: $data", e)
                    null
                } catch (e: IOException) {
                    Log.e("VideoThumbnailFetcher", "IO error setting data source from Uri: $data", e)
                    null
                }
            }

            is File -> {
                try {
                    retriever.setDataSource(data.absolutePath)
                    true
                } catch (e: IllegalStateException) {
                    Log.e("VideoThumbnailFetcher", "Failed to set data source from File: ${data.absolutePath}", e)
                    null
                } catch (e: IOException) {
                    Log.e("VideoThumbnailFetcher", "IO error setting data source from File: ${data.absolutePath}", e)
                    null
                }
            }

            is String -> setDataSourceFromString(retriever, data)
            else -> null
        }
    }

    private fun setDataSourceFromString(
        retriever: MediaMetadataRetrieverWrapper,
        filePath: String
    ): Boolean? {
        val uri = runCatching { filePath.toUri() }.getOrNull()

        return try {
            if (uri?.scheme != null) {
                retriever.setDataSource(options.context, uri)
            } else {
                retriever.setDataSource(filePath)
            }
            true
        } catch (e: IllegalStateException) {
            Log.e("VideoThumbnailFetcher", "Failed to set data source: $filePath", e)
            null
        } catch (e: IOException) {
            Log.e("VideoThumbnailFetcher", "IO error setting data source: $filePath", e)
            null
        }
    }

    class Factory(
        private val videoFileDetector: VideoFileDetector = VideoFileDetector(),
        private val retrieverFactory: () -> MediaMetadataRetrieverWrapper = { MediaMetadataRetrieverWrapperImpl() }
    ) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): Fetcher? {
            return if (videoFileDetector.isVideoFile(data)) {
                VideoThumbnailFetcher(data, options, retrieverFactory)
            } else {
                null
            }
        }
    }

    companion object {
        private const val THUMBNAIL_TIME_MICROSECONDS = 1000L
    }
}
