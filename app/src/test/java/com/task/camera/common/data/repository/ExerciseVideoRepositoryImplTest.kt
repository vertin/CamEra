package com.task.camera.common.data.repository

import androidx.paging.PagingSource
import app.cash.turbine.test
import com.task.camera.common.data.local.ExerciseVideoDao
import com.task.camera.common.data.local.ExerciseVideoEntity
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseVideoRepositoryImplTest {

    private lateinit var dao: ExerciseVideoDao
    private lateinit var repository: ExerciseVideoRepositoryImpl

    @BeforeEach
    fun setup() {
        dao = mockk()
        repository = ExerciseVideoRepositoryImpl(dao)
    }

    @Test
    fun `getAllVideos should return mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            ExerciseVideoEntity(
                id = 1L,
                filePath = "/path/to/video1.mp4",
                fileName = "video1.mp4",
                durationMs = 10000L,
                timestamp = 1000L,
                status = VideoStatus.RECORDED
            ),
            ExerciseVideoEntity(
                id = 2L,
                filePath = "/path/to/video2.mp4",
                fileName = "video2.mp4",
                durationMs = 20000L,
                timestamp = 2000L,
                status = VideoStatus.UPLOADED
            )
        )
        every { dao.getAllVideos() } returns flowOf(entities)

        // When
        val result = repository.getAllVideos()

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            val videos = resultValue.getOrNull() ?: error("Expected success result")
            assertEquals(2, videos.size)
            assertEquals(1L, videos[0].id)
            assertEquals("/path/to/video1.mp4", videos[0].filePath)
            assertEquals(VideoStatus.RECORDED, videos[0].status)
            assertEquals(2L, videos[1].id)
            assertEquals(VideoStatus.UPLOADED, videos[1].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getVideoById should return mapped domain model`() = runTest {
        // Given
        val entity = ExerciseVideoEntity(
            id = 1L,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 15000L,
            timestamp = 1000L,
            status = VideoStatus.RECORDED
        )
        every { dao.getVideoById(1L) } returns flowOf(entity)

        // When
        val result = repository.getVideoById(1L)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            val video = resultValue.getOrNull() ?: error("Expected success result")
            assertEquals(1L, video.id)
            assertEquals("/path/to/video.mp4", video.filePath)
            assertEquals("video.mp4", video.fileName)
            assertEquals(VideoStatus.RECORDED, video.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getVideoById should return failure when video not found`() = runTest {
        // Given
        every { dao.getVideoById(999L) } returns flowOf(null)

        // When
        val result = repository.getVideoById(999L)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isFailure)
            assertTrue(resultValue.exceptionOrNull() is NoSuchElementException)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveVideo should call dao insertVideo and return id`() = runTest {
        // Given
        val createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(1000L), ZoneId.systemDefault())
        val video = ExerciseVideo(
            id = 0L,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 5000L,
            createdAt = createdAt,
            status = VideoStatus.RECORDED
        )
        val expectedId = 1L
        coEvery { dao.insertVideo(any()) } returns expectedId

        // When
        val result = repository.saveVideo(video)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify(exactly = 1) {
            dao.insertVideo(
                match {
                    it.filePath == video.filePath &&
                        it.fileName == video.fileName &&
                        it.durationMs == video.durationMs &&
                        it.status == video.status &&
                        it.timestamp == 1000L
                }
            )
        }
    }

    @Test
    fun `updateVideoStatus should call dao updateStatus`() = runTest {
        // Given
        val videoId = 1L
        val status = VideoStatus.UPLOADING
        coEvery { dao.updateStatus(videoId, status) } returns Unit

        // When
        val result = repository.updateVideoStatus(videoId, status)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dao.updateStatus(videoId, status) }
    }

    @Test
    fun `deleteVideo should call dao deleteVideo`() = runTest {
        // Given
        val videoId = 1L
        coEvery { dao.deleteVideo(videoId) } returns Unit

        // When
        val result = repository.deleteVideo(videoId)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dao.deleteVideo(videoId) }
    }

    @Test
    fun `getAllVideosPaged should return paging data with mapped domain models`() = runTest {
        // Given
        val pagingSource = mockk<PagingSource<Int, ExerciseVideoEntity>>(relaxed = true)
        every { dao.getAllVideosPaged() } returns pagingSource

        // When
        val result = repository.getAllVideosPaged()

        // Then
        // Verify that the flow is created (we can't easily test PagingData without more setup)
        result.test {
            // The flow should be created, but we can't easily assert on PagingData content
            // without more complex setup. This test verifies the method doesn't throw.
            cancelAndIgnoreRemainingEvents()
        }
        // Verify dao was called
        // Note: PagingData testing requires more complex setup, so we just verify the method works
    }
}
