package com.task.camera.recorder.presentation.domain

import com.task.camera.common.domain.model.Metadata
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoMetadataService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SaveVideoFileUseCaseTest {

    private lateinit var repository: ExerciseVideoRepository
    private lateinit var metadataService: VideoMetadataService
    private lateinit var useCase: SaveVideoFileUseCase

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        metadataService = mockk(relaxed = true)
        useCase = SaveVideoFileUseCase(repository, metadataService)
    }

    @Test
    fun `launch should extract metadata and save video with correct properties`() = runTest {
        // Given
        val filePath = "/path/to/video.mp4"
        val fileName = "test_video.mp4"
        val durationMs = 10000L
        val metadata = Metadata(durationMs = durationMs)
        val expectedVideoId = 1L

        coEvery { metadataService.extractMetadata(filePath) } returns Result.success(metadata)
        coEvery { repository.saveVideo(any()) } returns Result.success(expectedVideoId)

        // When
        val result = useCase.launch(filePath, fileName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
        coVerify(exactly = 1) { metadataService.extractMetadata(filePath) }
        coVerify(exactly = 1) {
            repository.saveVideo(
                match { video ->
                    video.filePath == filePath &&
                        video.fileName == fileName &&
                        video.durationMs == durationMs &&
                        video.status == VideoStatus.RECORDED
                }
            )
        }
    }

    @Test
    fun `launch should use default duration when metadata extraction fails`() = runTest {
        // Given
        val filePath = "/path/to/video.mp4"
        val fileName = "test_video.mp4"
        val expectedVideoId = 1L
        val metadataError = RuntimeException("Failed to extract metadata")

        coEvery { metadataService.extractMetadata(filePath) } returns Result.failure(metadataError)
        coEvery { repository.saveVideo(any()) } returns Result.success(expectedVideoId)

        // When
        val result = useCase.launch(filePath, fileName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedVideoId, result.getOrNull())
        coVerify(exactly = 1) { metadataService.extractMetadata(filePath) }
        coVerify(exactly = 1) {
            repository.saveVideo(
                match { video ->
                    video.filePath == filePath &&
                        video.fileName == fileName &&
                        video.durationMs == 0L &&
                        video.status == VideoStatus.RECORDED
                }
            )
        }
    }

    @Test
    fun `launch should return failure when repository save fails`() = runTest {
        // Given
        val filePath = "/path/to/video.mp4"
        val fileName = "test_video.mp4"
        val metadata = Metadata(durationMs = 5000L)
        val databaseError = RuntimeException("Database error")

        coEvery { metadataService.extractMetadata(filePath) } returns Result.success(metadata)
        coEvery { repository.saveVideo(any()) } returns Result.failure(databaseError)

        // When
        val result = useCase.launch(filePath, fileName)

        // Then - should return failure Result
        assertTrue(result.isFailure)
        assertEquals(databaseError, result.exceptionOrNull())

        coVerify(exactly = 1) { metadataService.extractMetadata(filePath) }
        coVerify(exactly = 1) { repository.saveVideo(any()) }
    }

    @Test
    fun `launch should set createdAt to current time`() = runTest {
        // Given
        val filePath = "/path/to/video.mp4"
        val fileName = "test_video.mp4"
        val beforeTime = LocalDateTime.now()
        val metadata = Metadata(durationMs = 10000L)

        coEvery { metadataService.extractMetadata(filePath) } returns Result.success(metadata)
        coEvery { repository.saveVideo(any()) } returns Result.success(1L)

        // When
        val result = useCase.launch(filePath, fileName)
        val afterTime = LocalDateTime.now()

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            repository.saveVideo(
                match { video ->
                    video.createdAt.isAfter(beforeTime) || video.createdAt.isEqual(beforeTime) &&
                        video.createdAt.isBefore(afterTime) || video.createdAt.isEqual(afterTime)
                }
            )
        }
    }
}
