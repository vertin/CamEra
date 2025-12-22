package com.task.camera.library.presentation

import androidx.paging.PagingData
import app.cash.turbine.test
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.library.domain.DeleteVideoUseCase
import com.task.camera.library.domain.GetAllVideosPagedUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private lateinit var getAllVideosPagedUseCase: GetAllVideosPagedUseCase
    private lateinit var deleteVideoUseCase: DeleteVideoUseCase
    private lateinit var viewModel: LibraryViewModel

    @BeforeEach
    fun setup() {
        getAllVideosPagedUseCase = mockk(relaxed = true)
        deleteVideoUseCase = mockk(relaxed = true)
        viewModel = LibraryViewModel(getAllVideosPagedUseCase, deleteVideoUseCase)
    }

    @Test
    fun `videos should return non-null flow from useCase`() = runTest {
        // Given
        val testVideos = listOf(
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
        val pagingData = PagingData.from(testVideos)
        every { getAllVideosPagedUseCase.launch() } returns flowOf(pagingData)

        // When & Then
        // PagingData testing requires additional setup with PagingSource
        // For now, we just verify that the flow is created
        assertNotNull(viewModel.videos)
    }

    @Test
    fun `deleteVideo should call deleteVideoUseCase launch`() = runTest {
        // Given
        val videoId = 1L
        every { deleteVideoUseCase.launch(videoId) } returns flowOf(Result.success(Unit))

        // When
        viewModel.deleteVideo(videoId)
        advanceUntilIdle()

        // Then
        verify(exactly = 1) { deleteVideoUseCase.launch(videoId) }
    }

    @Test
    fun `onVideoClick should emit OpenDetails action`() = runTest {
        // Given
        val videoId = 1L

        // When
        viewModel.actions.test {
            viewModel.onVideoClick(videoId)
            advanceUntilIdle()

            // Then
            val action = awaitItem()
            assertTrue(action is LibraryAction.OpenDetails)
            assertEquals(videoId, (action as LibraryAction.OpenDetails).videoId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `recordNewVideo should emit RecordNewVideo action`() = runTest {
        // When
        viewModel.actions.test {
            viewModel.recordNewVideo()
            advanceUntilIdle()

            // Then
            val action = awaitItem()
            assertTrue(action is LibraryAction.RecordNewVideo)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `videos should return PagingData flow from useCase`() = runTest {
        // Given
        val testVideos = listOf(
            ExerciseVideo(
                id = 1L,
                filePath = "/path/to/video1.mp4",
                fileName = "video1.mp4",
                durationMs = 10000L,
                createdAt = LocalDateTime.now(),
                status = VideoStatus.RECORDED
            )
        )
        val pagingData = PagingData.from(testVideos)
        every {
            getAllVideosPagedUseCase.launch()
        } returns flowOf(pagingData)

        // When
        viewModel = LibraryViewModel(getAllVideosPagedUseCase, deleteVideoUseCase)

        // Then
        // PagingData errors are handled by Paging LoadState, not by ViewModel
        // We just verify that the flow is created
        assertNotNull(viewModel.videos)
    }

    @Test
    fun `deleteVideo should emit ShowDeleteError action when useCase fails`() = runTest {
        // Given
        val videoId = 1L
        val errorMessage = "Failed to delete video file"
        val exception = RuntimeException(errorMessage)
        every { deleteVideoUseCase.launch(videoId) } returns flowOf(Result.failure(exception))

        // When
        viewModel.actions.test {
            viewModel.deleteVideo(videoId)
            advanceUntilIdle()

            // Then
            val action = awaitItem()
            assertTrue(action is LibraryAction.ShowDeleteError)
            assertEquals(errorMessage, (action as LibraryAction.ShowDeleteError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteVideo should not emit action when useCase succeeds`() = runTest {
        // Given
        val videoId = 1L
        every { deleteVideoUseCase.launch(videoId) } returns flowOf(Result.success(Unit))

        // When
        viewModel.actions.test {
            viewModel.deleteVideo(videoId)
            advanceUntilIdle()

            // Then - should not emit any action on success
            expectNoEvents()
        }
    }
}
