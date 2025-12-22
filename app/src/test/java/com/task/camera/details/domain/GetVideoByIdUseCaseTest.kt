package com.task.camera.details.domain

import app.cash.turbine.test
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoFileService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GetVideoByIdUseCaseTest {

    private lateinit var repository: ExerciseVideoRepository
    private lateinit var videoFileService: VideoFileService
    private lateinit var useCase: GetVideoByIdUseCase

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        videoFileService = mockk(relaxed = true)
        useCase = GetVideoByIdUseCase(repository, videoFileService)
    }

    @Test
    fun `launch should return video when file exists`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/path/to/video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.fileExists(filePath) } returns true

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            assertEquals(video, resultValue.getOrNull())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should return failure when video file does not exist`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/path/to/missing-video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "missing-video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.fileExists(filePath) } returns false
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isFailure)
            assertInstanceOf(IllegalStateException::class.java, resultValue.exceptionOrNull())
            assertEquals("Video file not found", resultValue.exceptionOrNull()?.message)
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should return failure when repository returns failure`() = runTest {
        // Given
        val videoId = 1L
        val exception = NoSuchElementException("Video not found")
        every { repository.getVideoById(videoId) } returns flowOf(Result.failure(exception))

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isFailure)
            assertEquals(exception, resultValue.exceptionOrNull())
            coVerify(exactly = 0) { repository.deleteVideo(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should handle content URI file paths`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "content://media/external/video/media/123"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.fileExists(filePath) } returns true

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            assertEquals(video, resultValue.getOrNull())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should delete video from database when file does not exist`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/path/to/missing-video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "missing-video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.fileExists(filePath) } returns false
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isFailure)
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should handle different video statuses`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/path/to/video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.UPLOADED
        )
        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.fileExists(filePath) } returns true

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            assertEquals(VideoStatus.UPLOADED, resultValue.getOrNull()?.status)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

