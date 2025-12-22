package com.task.camera.common.data.service

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoMetadataServiceImplTest {

    private lateinit var context: Context
    private lateinit var service: VideoMetadataServiceImpl

    @BeforeEach
    fun setup() {
        // Mock static classes
        mockkStatic(Log::class)
        mockkStatic(Uri::class)
        mockkStatic(MediaMetadataRetriever::class)
        mockkConstructor(MediaMetadataRetriever::class)

        // Setup Log mocks
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Setup context
        context = mockk(relaxed = true)

        // Setup MediaMetadataRetriever
        every {
            anyConstructed<MediaMetadataRetriever>().setDataSource(
                context,
                any()
            )
        } returns Unit
        every {
            anyConstructed<MediaMetadataRetriever>().release()
        } returns Unit

        service = VideoMetadataServiceImpl(context, Dispatchers.IO)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `extractMetadata should return Metadata with duration when URI is valid`() = runTest {
        // Given
        val filePath = "content://media/external/video/media/123"
        val uri = mockk<Uri>(relaxed = true)
        val durationString = "15000"
        val expectedDuration = 15000L

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns "content"
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } returns Unit
        every {
            anyConstructed<MediaMetadataRetriever>().extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
        } returns durationString
        every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

        // When
        val result = service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDuration, result.getOrNull()?.durationMs)
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) }
        verify(exactly = 1) {
            anyConstructed<MediaMetadataRetriever>().extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
        }
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }

    @Test
    fun `extractMetadata should use file path when URI has no scheme`() = runTest {
        // Given
        val filePath = "/storage/emulated/0/DCIM/video.mp4"
        val uri = mockk<Uri>(relaxed = true)
        val durationString = "20000"
        val expectedDuration = 20000L

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns null
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(filePath) } returns Unit
        every {
            anyConstructed<MediaMetadataRetriever>().extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
        } returns durationString
        every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

        // When
        val result = service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDuration, result.getOrNull()?.durationMs)
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().setDataSource(filePath) }
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }

    @Test
    fun `extractMetadata should return Metadata with zero duration when duration is null`() =
        runTest {
            // Given
            val filePath = "content://media/external/video/media/123"
            val uri = mockk<Uri>(relaxed = true)

            every { Uri.parse(filePath) } returns uri
            every { uri.scheme } returns "content"
            every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } returns Unit
            every {
                anyConstructed<MediaMetadataRetriever>().extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
            } returns null
            every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

            // When
            val result = service.extractMetadata(filePath)
            advanceUntilIdle()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(0L, result.getOrNull()?.durationMs)
            verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
        }

    @Test
    fun `extractMetadata should return Metadata with zero duration when duration is invalid`() =
        runTest {
            // Given
            val filePath = "content://media/external/video/media/123"
            val uri = mockk<Uri>(relaxed = true)

            every { Uri.parse(filePath) } returns uri
            every { uri.scheme } returns "content"
            every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } returns Unit
            every {
                anyConstructed<MediaMetadataRetriever>().extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
            } returns "invalid"
            every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

            // When
            val result = service.extractMetadata(filePath)
            advanceUntilIdle()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(0L, result.getOrNull()?.durationMs)
            verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
        }

    @Test
    fun `extractMetadata should handle IllegalArgumentException when parsing URI and use file path`() =
        runTest {
            // Given
            val filePath = "invalid://path"
            val exception = IllegalArgumentException("Invalid URI format")
            val durationString = "5000"

            every { Uri.parse(filePath) } throws exception
            every { anyConstructed<MediaMetadataRetriever>().setDataSource(filePath) } returns Unit
            every {
                anyConstructed<MediaMetadataRetriever>().extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
            } returns durationString
            every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

            // When
            val result = service.extractMetadata(filePath)
            advanceUntilIdle()

            // Then - runCatching catches IllegalArgumentException and returns null, so code continues with filePath
            assertTrue(result.isSuccess)
            assertEquals(5000L, result.getOrNull()?.durationMs)
            verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().setDataSource(filePath) }
            verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
        }

    @Test
    fun `extractMetadata should return failure when IllegalStateException occurs during setDataSource`() =
        runTest {
            // Given
            val filePath = "content://media/external/video/media/123"
            val uri = mockk<Uri>(relaxed = true)
            val exception = IllegalStateException("Failed to set data source")

            every { Uri.parse(filePath) } returns uri
            every { uri.scheme } returns "content"
            every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } throws exception
            every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

            // When
            val result = service.extractMetadata(filePath)
            advanceUntilIdle()

            // Then
            assertTrue(result.isFailure)
            verify(exactly = 1) {
                Log.e(
                    "VideoMetadataService",
                    "Failed to extract metadata from: $filePath",
                    exception
                )
            }
            verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
        }

    @Test
    fun `extractMetadata should return failure when IOException occurs`() = runTest {
        // Given
        val filePath = "/storage/emulated/0/DCIM/video.mp4"
        val uri = mockk<Uri>(relaxed = true)
        val exception = Exception("IO error")

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns null
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(filePath) } throws exception
        every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

        // When
        val result = service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
        verify(exactly = 1) {
            Log.e(
                "VideoMetadataService",
                "Failed to extract metadata from: $filePath",
                exception
            )
        }
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }

    @Test
    fun `extractMetadata should return failure when RuntimeException occurs`() = runTest {
        // Given
        val filePath = "content://media/external/video/media/123"
        val uri = mockk<Uri>(relaxed = true)
        val exception = RuntimeException("Runtime error")

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns "content"
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } throws exception
        every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

        // When
        val result = service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
        verify(exactly = 1) {
            Log.e(
                "VideoMetadataService",
                "Failed to extract metadata from: $filePath",
                exception
            )
        }
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }

    @Test
    fun `extractMetadata should return failure when OutOfMemoryError occurs`() = runTest {
        // Given
        val filePath = "content://media/external/video/media/123"
        val uri = mockk<Uri>(relaxed = true)
        val exception = OutOfMemoryError("Out of memory")

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns "content"
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } throws exception
        every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

        // When
        val result = service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
        verify(exactly = 1) {
            Log.e(
                "VideoMetadataService",
                "Out of memory error: $filePath",
                exception
            )
        }
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }

    @Test
    fun `extractMetadata should always call release even when exception occurs`() = runTest {
        // Given
        val filePath = "content://media/external/video/media/123"
        val uri = mockk<Uri>(relaxed = true)
        val exception = RuntimeException("Error")

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns "content"
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } throws exception
        every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

        // When
        service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then - verify release is called even when exception occurs
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }

    @Test
    fun `extractMetadata should handle IllegalArgumentException from setDataSource and return null`() =
        runTest {
            // Given
            val filePath = "content://media/external/video/media/123"
            val uri = mockk<Uri>(relaxed = true)
            val exception = IllegalArgumentException("Invalid data source")

            every { Uri.parse(filePath) } returns uri
            every { uri.scheme } returns "content"
            every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } throws exception
            every { anyConstructed<MediaMetadataRetriever>().release() } returns Unit

            // When
            val result = service.extractMetadata(filePath)
            advanceUntilIdle()

            // Then
            assertTrue(result.isFailure)
            verify(exactly = 1) {
                Log.d(
                    "VideoMetadataService",
                    "Invalid URI format: $filePath",
                    exception
                )
            }
            verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
        }

    @Test
    fun `extractMetadata should handle exception in release and still return result`() = runTest {
        // Given
        val filePath = "content://media/external/video/media/123"
        val uri = mockk<Uri>(relaxed = true)
        val durationString = "10000"

        every { Uri.parse(filePath) } returns uri
        every { uri.scheme } returns "content"
        every { anyConstructed<MediaMetadataRetriever>().setDataSource(context, uri) } returns Unit
        every {
            anyConstructed<MediaMetadataRetriever>().extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
        } returns durationString
        every { anyConstructed<MediaMetadataRetriever>().release() } throws Exception("Release error")

        // When
        val result = service.extractMetadata(filePath)
        advanceUntilIdle()

        // Then - should still return result even if release throws
        assertTrue(result.isSuccess)
        assertEquals(10000L, result.getOrNull()?.durationMs)
        verify(exactly = 1) { anyConstructed<MediaMetadataRetriever>().release() }
    }
}
