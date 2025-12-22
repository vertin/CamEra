package com.task.camera.library.domain

import androidx.paging.PagingData
import app.cash.turbine.test
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GetAllVideosPagedUseCaseTest {

    private lateinit var repository: ExerciseVideoRepository
    private lateinit var useCase: GetAllVideosPagedUseCase

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = GetAllVideosPagedUseCase(repository)
    }

    @Test
    fun `launch should return paging data from repository`() = runTest {
        // Given
        val videos = listOf(
            ExerciseVideo(
                id = 1L,
                filePath = "/path/to/video1.mp4",
                fileName = "video1.mp4",
                durationMs = 10000L,
                createdAt = LocalDateTime.now(),
                status = VideoStatus.RECORDED
            ),
            ExerciseVideo(
                id = 2L,
                filePath = "/path/to/video2.mp4",
                fileName = "video2.mp4",
                durationMs = 20000L,
                createdAt = LocalDateTime.now(),
                status = VideoStatus.UPLOADED
            )
        )
        val pagingData = PagingData.from(videos)
        every { repository.getAllVideosPaged() } returns flowOf(pagingData)

        // When
        val result = useCase.launch()

        // Then
        result.test {
            val data = awaitItem()
            assertNotNull(data)
            verify(exactly = 1) { repository.getAllVideosPaged() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should delegate to repository getAllVideosPaged`() = runTest {
        // Given
        val emptyPagingData = PagingData.empty<ExerciseVideo>()
        every { repository.getAllVideosPaged() } returns flowOf(emptyPagingData)

        // When
        val result = useCase.launch()

        // Then
        result.test {
            awaitItem()
            verify(exactly = 1) { repository.getAllVideosPaged() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch should return flow that emits paging data`() = runTest {
        // Given
        val video = ExerciseVideo(
            id = 1L,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 5000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        val pagingData = PagingData.from(listOf(video))
        every { repository.getAllVideosPaged() } returns flowOf(pagingData)

        // When
        val result = useCase.launch()

        // Then
        result.test {
            val data = awaitItem()
            assertNotNull(data)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

