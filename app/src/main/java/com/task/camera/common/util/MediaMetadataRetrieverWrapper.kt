package com.task.camera.common.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * Wrapper interface for MediaMetadataRetriever to enable mocking in tests
 */
interface MediaMetadataRetrieverWrapper {
    /**
     * Sets data source from URI
     */
    fun setDataSource(context: Context, uri: Uri)

    /**
     * Sets data source from file path
     */
    fun setDataSource(path: String)

    /**
     * Gets frame from video at specified time
     */
    fun getFrameAtTime(timeUs: Long, option: Int): Bitmap?

    /**
     * Releases resources
     */
    fun release()
}

/**
 * Implementation of wrapper for MediaMetadataRetriever
 */
class MediaMetadataRetrieverWrapperImpl : MediaMetadataRetrieverWrapper {
    private val retriever = MediaMetadataRetriever()

    override fun setDataSource(context: Context, uri: Uri) {
        retriever.setDataSource(context, uri)
    }

    override fun setDataSource(path: String) {
        retriever.setDataSource(path)
    }

    override fun getFrameAtTime(timeUs: Long, option: Int): Bitmap? {
        return retriever.getFrameAtTime(timeUs, option)
    }

    override fun release() {
        retriever.release()
    }
}
