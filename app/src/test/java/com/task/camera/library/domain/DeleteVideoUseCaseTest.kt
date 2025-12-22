package com.task.camera.library.domain

import app.cash.turbine.test
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoFileService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteVideoUseCaseTest {

    private lateinit var repository: ExerciseVideoRepository
    private lateinit var videoFileService: VideoFileService
    private lateinit var useCase: DeleteVideoUseCase

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        videoFileService = mockk(relaxed = true)
        useCase = DeleteVideoUseCase(repository, videoFileService)
    }

    @Test
    fun `launch should delete video file and remove from database`() = runTest {
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
        every { videoFileService.deleteFile(filePath) } just runs
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should delete video file with regular file path`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/storage/emulated/0/Movies/video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )

        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.deleteFile(filePath) } just runs
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should succeed even when file service handles errors internally`() = runTest {
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
        every { videoFileService.deleteFile(filePath) } just runs
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should return failure when video not found`() = runTest {
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
            assertTrue(resultValue.exceptionOrNull() is NoSuchElementException)
            coVerify(exactly = 0) { repository.deleteVideo(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should return failure when database delete fails`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/storage/emulated/0/Movies/video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        val dbException = RuntimeException("Database error")

        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.deleteFile(filePath) } just runs
        coEvery { repository.deleteVideo(videoId) } returns Result.failure(dbException)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isFailure)
            assertEquals(dbException, resultValue.exceptionOrNull())
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should succeed even when file delete fails - file might not exist`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "/storage/emulated/0/Movies/video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        val fileException = IllegalStateException("Failed to delete video file: $filePath")

        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.deleteFile(filePath) } throws fileException
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        // File deletion failure is ignored - video might be in DB but file missing
        // Database deletion should still succeed
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess, "Should succeed even if file deletion fails")
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should handle any file path format`() = runTest {
        // Given
        val videoId = 1L
        val filePath = "invalid-uri"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )

        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        every { videoFileService.deleteFile(filePath) } just runs
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess)
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should delete from database even when file is missing`() = runTest {
        // Given - video exists in database but file is missing
        val videoId = 1L
        val filePath = "/storage/emulated/0/Movies/missing-video.mp4"
        val video = ExerciseVideo(
            id = videoId,
            filePath = filePath,
            fileName = "missing-video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )

        every { repository.getVideoById(videoId) } returns flowOf(Result.success(video))
        // File service handles missing file gracefully (doesn't throw)
        every { videoFileService.deleteFile(filePath) } just runs
        coEvery { repository.deleteVideo(videoId) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        // Should succeed - delete from DB even if file doesn't exist
        result.test {
            val resultValue = awaitItem()
            assertTrue(resultValue.isSuccess, "Should succeed when file is missing")
            verify(exactly = 1) { videoFileService.deleteFile(filePath) }
            coVerify(exactly = 1) { repository.deleteVideo(videoId) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
