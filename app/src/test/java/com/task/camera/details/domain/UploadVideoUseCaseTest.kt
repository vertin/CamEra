package com.task.camera.details.domain

import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.repository.UploadVideoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UploadVideoUseCaseTest {

    private lateinit var uploadVideoRepository: UploadVideoRepository
    private lateinit var exerciseVideoRepository: ExerciseVideoRepository
    private lateinit var useCase: UploadVideoUseCase

    @BeforeEach
    fun setup() {
        uploadVideoRepository = mockk(relaxed = true)
        exerciseVideoRepository = mockk(relaxed = true)
        useCase = UploadVideoUseCase(uploadVideoRepository, exerciseVideoRepository)
    }

    @Test
    fun `launch should upload video and update status to UPLOADED`() = runTest {
        // Given
        val videoId = 1L
        coEvery { uploadVideoRepository.uploadVideo() } returns Result.success(Unit)
        coEvery { exerciseVideoRepository.updateVideoStatus(videoId, VideoStatus.UPLOADED) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { uploadVideoRepository.uploadVideo() }
        coVerify(exactly = 1) { exerciseVideoRepository.updateVideoStatus(videoId, VideoStatus.UPLOADED) }
    }

    @Test
    fun `launch should return failure when upload fails`() = runTest {
        // Given
        val videoId = 1L
        val uploadException = RuntimeException("Network error")
        coEvery { uploadVideoRepository.uploadVideo() } returns Result.failure(uploadException)

        // When
        val result = useCase.launch(videoId)

        // Then
        assertTrue(result.isFailure)
        assertInstanceOf(UploadVideoException::class.java, result.exceptionOrNull())
        assertEquals("Failed to upload video for videoId: $videoId", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { uploadVideoRepository.uploadVideo() }
        coVerify(exactly = 0) { exerciseVideoRepository.updateVideoStatus(any(), any()) }
    }

    @Test
    fun `launch should return failure when status update fails`() = runTest {
        // Given
        val videoId = 1L
        val dbException = RuntimeException("Database error")
        coEvery { uploadVideoRepository.uploadVideo() } returns Result.success(Unit)
        coEvery { exerciseVideoRepository.updateVideoStatus(videoId, VideoStatus.UPLOADED) } returns Result.failure(dbException)

        // When
        val result = useCase.launch(videoId)

        // Then
        assertTrue(result.isFailure)
        assertInstanceOf(UploadVideoException::class.java, result.exceptionOrNull())
        assertEquals("Failed to update video status to UPLOADED for videoId: $videoId", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { uploadVideoRepository.uploadVideo() }
        coVerify(exactly = 1) { exerciseVideoRepository.updateVideoStatus(videoId, VideoStatus.UPLOADED) }
    }

    @Test
    fun `launch should handle multiple video uploads`() = runTest {
        // Given
        val videoId1 = 1L
        val videoId2 = 2L
        coEvery { uploadVideoRepository.uploadVideo() } returns Result.success(Unit)
        coEvery { exerciseVideoRepository.updateVideoStatus(videoId1, VideoStatus.UPLOADED) } returns Result.success(Unit)
        coEvery { exerciseVideoRepository.updateVideoStatus(videoId2, VideoStatus.UPLOADED) } returns Result.success(Unit)

        // When
        val result1 = useCase.launch(videoId1)
        val result2 = useCase.launch(videoId2)

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        coVerify(exactly = 2) { uploadVideoRepository.uploadVideo() }
        coVerify(exactly = 1) { exerciseVideoRepository.updateVideoStatus(videoId1, VideoStatus.UPLOADED) }
        coVerify(exactly = 1) { exerciseVideoRepository.updateVideoStatus(videoId2, VideoStatus.UPLOADED) }
    }
}
