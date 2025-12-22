package com.task.camera.recorder.presentation

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import app.cash.turbine.test
import com.task.camera.recorder.camera.CameraManager
import com.task.camera.recorder.camera.VideoRecordingResult
import com.task.camera.recorder.presentation.domain.SaveVideoFileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecorderViewModelTest {

    private lateinit var saveVideoFileUseCase: SaveVideoFileUseCase
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraSliceFlow: MutableStateFlow<CameraSlice?>
    private lateinit var viewModel: RecorderViewModel

    @BeforeEach
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        saveVideoFileUseCase = mockk(relaxed = true)
        cameraSliceFlow = MutableStateFlow(null)
        cameraManager = mockk(relaxed = true) {
            every { cameraSlice } returns cameraSliceFlow
        }
        viewModel = RecorderViewModel(saveVideoFileUseCase, cameraManager)
    }

    @Test
    fun `initial state should be Initialization`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(UiState.Initialization, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should be NeedPermissions when permission is OnRequest`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is UiState.NeedPermissions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `permissionGranted should update uiState to RecorderUiState`() = runTest {
        cameraSliceFlow.value = CameraSlice()

        viewModel.permissionGranted()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is UiState.RecorderUiState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startRecording should call cameraManager startVideoRecording`() = runTest {
        viewModel.startRecording()

        verify(exactly = 1) { cameraManager.startVideoRecording() }
    }

    @Test
    fun `bind should call cameraManager bindToLifecycle`() = runTest(StandardTestDispatcher()) {
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)
        coEvery { cameraManager.bindToLifecycle(any()) } returns Unit

        viewModel.bind(lifecycleOwner)
        advanceUntilIdle()

        coVerify(exactly = 1) { cameraManager.bindToLifecycle(lifecycleOwner) }
    }

    @Test
    fun `stopRecording should call cameraManager stopVideoRecording and save video when result is not null`() =
        runTest(StandardTestDispatcher()) {
            val filePath = "content://media/external/video/media/123"
            val fileName = "Video_1234567890.mp4"
            val result = VideoRecordingResult(filePath, fileName)

            coEvery {
                cameraManager.stopVideoRecording()
            } answers {
                result
            }
            coEvery { saveVideoFileUseCase.launch(any(), any()) } returns Result.success(1L)

            viewModel.stopRecording()
            advanceUntilIdle()

            coVerify(exactly = 1) { cameraManager.stopVideoRecording() }
            coVerify(exactly = 1) {
                saveVideoFileUseCase.launch(filePath, fileName)
            }
        }

    @Test
    fun `stopRecording should not save video when result is null`() =
        runTest(StandardTestDispatcher()) {
            coEvery { cameraManager.stopVideoRecording() } returns null

            viewModel.stopRecording()
            advanceUntilIdle()

            coVerify(exactly = 1) { cameraManager.stopVideoRecording() }
            coVerify(exactly = 0) { saveVideoFileUseCase.launch(any(), any()) }
        }

    @Test
    fun `stopRecording should call saveVideoFileUseCase even when save fails`() = runTest(StandardTestDispatcher()) {
        val filePath = "content://media/external/video/media/123"
        val fileName = "Video_1234567890.mp4"
        val result = VideoRecordingResult(filePath, fileName)

        coEvery {
            cameraManager.stopVideoRecording()
        } answers {
            result
        }
        // Exception is caught in UseCase, so it won't propagate to ViewModel
        // Use relaxed mock to allow exception to be caught
        coEvery {
            saveVideoFileUseCase.launch(
                any(),
                any()
            )
        } returns Result.failure(RuntimeException("Database error"))

        // When - exception is handled in UseCase, ViewModel should continue normally
        // The exception is caught inside the coroutine in saveVideo(), so it won't crash the test
        viewModel.stopRecording()
        advanceUntilIdle()

        // Then - verify both methods were called despite the error
        coVerify(exactly = 1) { cameraManager.stopVideoRecording() }
        coVerify(exactly = 1) { saveVideoFileUseCase.launch(filePath, fileName) }
    }

    @Test
    fun `uiState should be RecorderUiState when permission granted and camera slice available`() =
        runTest {
            val cameraSlice = CameraSlice(isRecording = false)
            cameraSliceFlow.value = cameraSlice

            viewModel.permissionGranted()

            viewModel.uiState.test {
                skipItems(1)
                val state = awaitItem()
                assertTrue(state is UiState.RecorderUiState)
                val recorderState = state as UiState.RecorderUiState
                assertEquals(cameraSlice, recorderState.cameraSlice)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState should reflect camera slice isRecording state`() = runTest {
        val cameraSlice = CameraSlice(isRecording = true)
        cameraSliceFlow.value = cameraSlice

        viewModel.permissionGranted()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is UiState.RecorderUiState)
            val recorderState = state as UiState.RecorderUiState
            assertTrue(recorderState.cameraSlice?.isRecording == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should update when camera slice changes`() = runTest {
        viewModel.permissionGranted()

        viewModel.uiState.test {
            skipItems(1)

            val initialSlice = CameraSlice(isRecording = false)
            cameraSliceFlow.value = initialSlice
            var state = awaitItem()
            assertTrue(state is UiState.RecorderUiState)
            assertTrue((state as UiState.RecorderUiState).cameraSlice?.isRecording == false)

            val updatedSlice = CameraSlice(isRecording = true)
            cameraSliceFlow.value = updatedSlice
            state = awaitItem()
            assertTrue(state is UiState.RecorderUiState)
            assertTrue((state as UiState.RecorderUiState).cameraSlice?.isRecording == true)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stopRecording should call saveVideoFileUseCase with correct file path and name`() =
        runTest(StandardTestDispatcher()) {
            val filePath = "content://media/external/video/media/123"
            val fileName = "Video_1234567890.mp4"
            val result = VideoRecordingResult(filePath, fileName)

            coEvery {
                cameraManager.stopVideoRecording()
            } returns
                result

            coEvery { saveVideoFileUseCase.launch(any(), any()) } returns Result.success(1L)

            viewModel.stopRecording()
            advanceUntilIdle()

            coVerify {
                saveVideoFileUseCase.launch(filePath, fileName)
            }
        }

    @Test
    fun `stopRecording should emit NavigateBack action when save succeeds`() = runTest(StandardTestDispatcher()) {
        val filePath = "content://media/external/video/media/123"
        val fileName = "Video_1234567890.mp4"
        val result = VideoRecordingResult(filePath, fileName)

        coEvery {
            cameraManager.stopVideoRecording()
        } returns result

        coEvery { saveVideoFileUseCase.launch(any(), any()) } returns Result.success(1L)

        viewModel.action.test {
            viewModel.stopRecording()
            advanceUntilIdle()

            val action = awaitItem()
            assertEquals(RecorderAction.NavigateBack, action)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should be NeedPermissions initially when permission not granted`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is UiState.NeedPermissions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should transition from NeedPermissions to RecorderUiState when permission granted`() =
        runTest {
            val cameraSlice = CameraSlice(isRecording = false)
            cameraSliceFlow.value = cameraSlice

            viewModel.uiState.test {
                skipItems(1)
                val needPermissionsState = awaitItem()
                assertTrue(needPermissionsState is UiState.NeedPermissions)

                viewModel.permissionGranted()
                val recorderState = awaitItem()
                assertTrue(recorderState is UiState.RecorderUiState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `stopRecording should emit ShowError when result is null`() =
        runTest(StandardTestDispatcher()) {
            coEvery { cameraManager.stopVideoRecording() } returns null

            viewModel.action.test {
                viewModel.stopRecording()
                advanceUntilIdle()

                val action = awaitItem()
                assertTrue(action is RecorderAction.ShowError)
                assertEquals("Failed to stop recording", (action as RecorderAction.ShowError).message)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
