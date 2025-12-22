package com.task.camera.common.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.request.Options
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Updated tests for VideoThumbnailFetcher after refactoring
 * Now the class uses VideoFileDetector and MediaMetadataRetrieverWrapper
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class VideoThumbnailFetcherRefactoredTest {

    private lateinit var context: Context
    private lateinit var imageLoader: ImageLoader
    private lateinit var options: Options
    private lateinit var videoFileDetector: VideoFileDetector

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        imageLoader = ImageLoader(context)
        options = Options(context)
        videoFileDetector = VideoFileDetector()
    }

    @Test
    fun factoryShouldCreateFetcherForMp4FilePath() {
        val filePath = "/storage/video.mp4"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(filePath, options, imageLoader)

        assertNotNull("Factory should create fetcher for .mp4 file", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherForMkvFilePath() {
        val filePath = "/storage/video.mkv"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(filePath, options, imageLoader)

        assertNotNull("Factory should create fetcher for .mkv file", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherForWebmFilePath() {
        val filePath = "/storage/video.webm"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(filePath, options, imageLoader)

        assertNotNull("Factory should create fetcher for .webm file", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherFor3gpFilePath() {
        val filePath = "/storage/video.3gp"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(filePath, options, imageLoader)

        assertNotNull("Factory should create fetcher for .3gp file", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherForCaseInsensitiveFileExtensions() {
        val filePaths = listOf(
            "/storage/video.MP4",
            "/storage/video.MKV",
            "/storage/video.WEBM",
            "/storage/video.3GP"
        )
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        filePaths.forEach { filePath ->
            val fetcher = factory.create(filePath, options, imageLoader)
            assertNotNull(
                "Factory should create fetcher for uppercase extension: $filePath",
                fetcher
            )
        }
    }

    @Test
    fun factoryShouldNotCreateFetcherForNonVideoFilePath() {
        val filePath = "/storage/image.jpg"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(filePath, options, imageLoader)

        assertNull("Factory should not create fetcher for non-video file", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherForContentUriWithVideoPath() {
        val contentUri = "content://media/external/video/media/123"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(contentUri, options, imageLoader)

        assertNotNull("Factory should create fetcher for content URI with /video/ path", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherForContentSchemeUri() {
        val contentUri = Uri.parse("content://media/external/images/media/123")
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(contentUri, options, imageLoader)

        assertNotNull("Factory should create fetcher for content scheme URI", fetcher)
    }

    @Test
    fun factoryShouldCreateFetcherForFileObjectWithVideoExtension() {
        val file = File("/storage/video.mp4")
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(file, options, imageLoader)

        assertNotNull("Factory should create fetcher for File with video extension", fetcher)
    }

    @Test
    fun factoryShouldNotCreateFetcherForFileObjectWithNonVideoExtension() {
        val file = File("/storage/image.png")
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(file, options, imageLoader)

        assertNull("Factory should not create fetcher for File with non-video extension", fetcher)
    }

    @Test
    fun factoryShouldNotCreateFetcherForUnsupportedDataType() {
        val data = 12345
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(data, options, imageLoader)

        assertNull("Factory should not create fetcher for unsupported data type", fetcher)
    }

    @Test
    fun factoryShouldHandleInvalidUriFormatGracefully() {
        val invalidUri = "not a valid uri://format"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)

        val fetcher = factory.create(invalidUri, options, imageLoader)

        assertNull("Factory should return null for invalid URI format without video extension", fetcher)
    }

    @Test
    fun fetcherShouldHandleNonExistentFilePathGracefully() {
        val nonExistentPath = "/non/existent/path/video.mp4"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)
        val fetcher = factory.create(nonExistentPath, options, imageLoader)

        val result = kotlinx.coroutines.runBlocking {
            fetcher?.fetch()
        }

        assertNull("Fetcher should return null for non-existent file", result)
    }

    @Test
    fun fetcherShouldHandleInvalidFilePathGracefully() {
        val invalidPath = ""
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector)
        val fetcher = factory.create(invalidPath, options, imageLoader)

        val result = kotlinx.coroutines.runBlocking {
            fetcher?.fetch()
        }

        assertTrue(
            "Fetcher should handle invalid file path without crashing",
            fetcher == null || result == null
        )
    }

    @Test
    fun fetcherShouldUseInjectedRetrieverFactory() {
        var factoryCalled = false
        val mockRetriever = object : MediaMetadataRetrieverWrapper {
            override fun setDataSource(context: Context, uri: Uri) {
                factoryCalled = true
            }

            override fun setDataSource(path: String) {
                factoryCalled = true
            }

            override fun getFrameAtTime(timeUs: Long, option: Int): Bitmap? = null
            override fun release() {}
        }

        val retrieverFactory = { mockRetriever }
        val filePath = "/storage/video.mp4"
        val factory = VideoThumbnailFetcher.Factory(videoFileDetector, retrieverFactory)
        val fetcher = factory.create(filePath, options, imageLoader)

        kotlinx.coroutines.runBlocking {
            fetcher?.fetch()
        }

        assertTrue("Fetcher should use injected retriever factory", factoryCalled)
    }
}
