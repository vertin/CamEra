package com.task.camera.details.presentation

import app.cash.turbine.test
import com.task.camera.common.data.mapper.MediaItemMapper
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.details.domain.GetVideoByIdUseCase
import com.task.camera.details.domain.SubmitVideoToUploadUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private lateinit var getVideoByIdUseCase: GetVideoByIdUseCase
    private lateinit var videoMapper: MediaItemMapper
    private lateinit var submitVideoToUploadUseCase: SubmitVideoToUploadUseCase
    private lateinit var viewModel: DetailsViewModel
    private val videoId = 1L

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getVideoByIdUseCase = mockk(relaxed = true)
        videoMapper = mockk(relaxed = true)
        submitVideoToUploadUseCase = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should load video and update to DetailsUiState when video loaded`() = runTest {
        // Given
        val testVideo = ExerciseVideo(
            id = videoId,
            filePath = "content://media/external/video/media/123",
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        val videoFile = mockk<VideoFile>()
        every { getVideoByIdUseCase.launch(videoId) } returns flowOf(Result.success(testVideo))
        every { videoMapper.map(testVideo) } returns videoFile

        viewModel = DetailsViewModel(
            getVideoByIdUseCase,
            videoMapper,
            submitVideoToUploadUseCase,
            videoId
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Loading)
            // Wait for the flow to process the video
            val loadedState = awaitItem()
            assertTrue(loadedState is UiState.DetailsUiState)
            assertEquals((loadedState as UiState.DetailsUiState).videoFile, videoFile)
            expectNoEvents()
        }
    }

    @Test
    fun `uiState should handle error and emit NavigateBack action when video load fails`() = runTest {
        // Given
        every { getVideoByIdUseCase.launch(videoId) } returns flowOf(
            Result.failure(
                IllegalStateException("Video not found")
            )
        )

        // When
        viewModel = DetailsViewModel(
            getVideoByIdUseCase,
            videoMapper,
            submitVideoToUploadUseCase,
            videoId
        )

        // Then
        viewModel.actions.test {
            viewModel.uiState.test {
                val loadingState = awaitItem()
                assertInstanceOf(UiState.Loading::class.java, loadingState)
                awaitItem()
            }
            val actionItem = awaitItem()
            assertInstanceOf(DetailsAction.NavigateBack::class.java, actionItem)
        }
    }

    @Test
    fun `uploadVideo should call submitVideoToUploadUseCase`() = runTest {
        // Given
        val testVideo = ExerciseVideo(
            id = videoId,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        every { getVideoByIdUseCase.launch(videoId) } returns flowOf(Result.success(testVideo))
        coEvery { submitVideoToUploadUseCase.launch(videoId) } returns Result.success(Unit)
        viewModel = DetailsViewModel(
            getVideoByIdUseCase,
            videoMapper,
            submitVideoToUploadUseCase,
            videoId
        )
        // When
        viewModel.uploadVideo()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { submitVideoToUploadUseCase.launch(videoId) }
    }

    @Test
    fun `uploadVideo should call submitVideoToUploadUseCase even if video not loaded yet`() =
        runTest {
            // Given
            coEvery { submitVideoToUploadUseCase.launch(videoId) } returns Result.success(Unit)
            viewModel = DetailsViewModel(
                getVideoByIdUseCase,
                videoMapper,
                submitVideoToUploadUseCase,
                videoId
            )

            // When
            viewModel.uploadVideo()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { submitVideoToUploadUseCase.launch(videoId) }
        }

    @Test
    fun `uiState should show Loading initially`() = runTest {
        // Given
        val testVideo = ExerciseVideo(
            id = videoId,
            filePath = "content://media/external/video/media/123",
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        every { getVideoByIdUseCase.launch(videoId) } returns flowOf(Result.success(testVideo))
        every { videoMapper.map(any()) } returns mockk<VideoFile>()

        // When
        viewModel = DetailsViewModel(
            getVideoByIdUseCase,
            videoMapper,
            submitVideoToUploadUseCase,
            videoId
        )

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uploadVideo should emit error action when useCase fails`() = runTest {
        // Given
        val testVideo = ExerciseVideo(
            id = videoId,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        val errorMessage = "Failed to upload video"
        val exception = RuntimeException(errorMessage)
        every { getVideoByIdUseCase.launch(videoId) } returns flowOf(Result.success(testVideo))
        coEvery { submitVideoToUploadUseCase.launch(videoId) } returns Result.failure(exception)

        viewModel = DetailsViewModel(
            getVideoByIdUseCase,
            videoMapper,
            submitVideoToUploadUseCase,
            videoId
        )

        // When
        viewModel.actions.test {
            viewModel.uploadVideo()
            advanceUntilIdle()

            // Then
            val action = awaitItem()
            assertTrue(action is DetailsAction.ShowSnackbar)
            assertEquals(errorMessage, (action as DetailsAction.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should update when video changes`() = runTest {
        // Given
        val testVideo1 = ExerciseVideo(
            id = videoId,
            filePath = "/path/to/video1.mp4",
            fileName = "video1.mp4",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )
        val testVideo2 = ExerciseVideo(
            id = videoId,
            filePath = "/path/to/video2.mp4",
            fileName = "video2.mp4",
            durationMs = 20000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.UPLOADED
        )
        val videoFile1 = mockk<VideoFile>()
        val videoFile2 = mockk<VideoFile>()

        every { getVideoByIdUseCase.launch(videoId) } returns flowOf(
            Result.success(testVideo1),
            Result.success(testVideo2)
        )
        every { videoMapper.map(testVideo1) } returns videoFile1
        every { videoMapper.map(testVideo2) } returns videoFile2

        // When
        viewModel = DetailsViewModel(
            getVideoByIdUseCase,
            videoMapper,
            submitVideoToUploadUseCase,
            videoId
        )

        // Then
        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            val state1 = awaitItem()
            assertTrue(state1 is UiState.DetailsUiState)
            assertEquals(videoFile1, (state1 as UiState.DetailsUiState).videoFile)

            val state2 = awaitItem()
            assertTrue(state2 is UiState.DetailsUiState)
            assertEquals(videoFile2, (state2 as UiState.DetailsUiState).videoFile)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
