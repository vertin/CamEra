package com.task.camera.details.domain

import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResetVideoStatusUseCaseTest {

    private lateinit var videoRepository: ExerciseVideoRepository
    private lateinit var useCase: ResetVideoStatusUseCase

    @BeforeEach
    fun setup() {
        videoRepository = mockk(relaxed = true)
        useCase = ResetVideoStatusUseCase(videoRepository)
    }

    @Test
    fun `launch should reset video status to RECORDED`() = runTest {
        // Given
        val videoId = 1L
        coEvery { videoRepository.updateVideoStatus(videoId, VideoStatus.RECORDED) } returns Result.success(Unit)

        // When
        val result = useCase.launch(videoId)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { videoRepository.updateVideoStatus(videoId, VideoStatus.RECORDED) }
    }

    @Test
    fun `launch should return failure when status update fails`() = runTest {
        // Given
        val videoId = 1L
        val dbException = RuntimeException("Database error")
        coEvery { videoRepository.updateVideoStatus(videoId, VideoStatus.RECORDED) } returns Result.failure(dbException)

        // When
        val result = useCase.launch(videoId)

        // Then
        assertTrue(result.isFailure)
        assertInstanceOf(ResetVideoStatusException::class.java, result.exceptionOrNull())
        assertEquals("Failed to reset video status for videoId: $videoId", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { videoRepository.updateVideoStatus(videoId, VideoStatus.RECORDED) }
    }

    @Test
    fun `launch should handle multiple video resets`() = runTest {
        // Given
        val videoId1 = 1L
        val videoId2 = 2L
        coEvery { videoRepository.updateVideoStatus(videoId1, VideoStatus.RECORDED) } returns Result.success(Unit)
        coEvery { videoRepository.updateVideoStatus(videoId2, VideoStatus.RECORDED) } returns Result.success(Unit)

        // When
        val result1 = useCase.launch(videoId1)
        val result2 = useCase.launch(videoId2)

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        coVerify(exactly = 1) { videoRepository.updateVideoStatus(videoId1, VideoStatus.RECORDED) }
        coVerify(exactly = 1) { videoRepository.updateVideoStatus(videoId2, VideoStatus.RECORDED) }
    }

    @Test
    fun `launch should preserve exception cause when status update fails`() = runTest {
        // Given
        val videoId = 1L
        val originalException = IllegalStateException("Database connection lost")
        coEvery { videoRepository.updateVideoStatus(videoId, VideoStatus.RECORDED) } returns Result.failure(originalException)

        // When
        val result = useCase.launch(videoId)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertInstanceOf(ResetVideoStatusException::class.java, exception)
        assertEquals(originalException, exception?.cause)
    }
}
