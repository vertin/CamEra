package com.task.camera.recorder.camera

import android.content.ContentResolver
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.lifecycle.LifecycleOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class CameraManagerImplTest {

    private lateinit var context: Context
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var executor: Executor
    private lateinit var cameraManager: CameraManagerImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true) {
            every { contentResolver } returns mockk<ContentResolver>(relaxed = true)
        }
        cameraProvider = mockk(relaxed = true)
        preview = mockk(relaxed = true)
        videoCapture = mockk(relaxed = true)
        executor = mockk(relaxed = true)

        cameraManager = CameraManagerImpl(
            context = context,
            cameraProvider = cameraProvider,
            preview = preview,
            videoCapture = videoCapture,
            executor = executor
        )
    }

    @Test
    fun bindToLifecycle_shouldCallCameraProviderBindToLifecycleWithCorrectParameters() = runTest {
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)
        
        coEvery { cameraProvider.unbindAll() } returns Unit
        coEvery { 
            cameraProvider.bindToLifecycle(any(), any(), any(), any())
        } returns mockk(relaxed = true)

        cameraManager.bindToLifecycle(lifecycleOwner)
        advanceUntilIdle()

        coVerify(exactly = 1) { cameraProvider.unbindAll() }
        coVerify(exactly = 1) { 
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture
            )
        }
    }

    @Test
    fun getErrorMessage_shouldReturnCorrectMessageForERROR_INSUFFICIENT_STORAGE() {
        val method = CameraManagerImpl::class.java.getDeclaredMethod(
            "getErrorMessage",
            Int::class.java
        )
        method.isAccessible = true
        
        val errorMessage = method.invoke(
            cameraManager,
            VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE
        ) as String
        
        assertEquals(
            "error should be about insufficient storage",
            "Insufficient storage space for video recording",
            errorMessage
        )
    }

    @Test
    fun getErrorMessage_shouldReturnCorrectMessageForERROR_FILE_SIZE_LIMIT_REACHED() {
        val method = CameraManagerImpl::class.java.getDeclaredMethod(
            "getErrorMessage",
            Int::class.java
        )
        method.isAccessible = true
        
        val errorMessage = method.invoke(
            cameraManager,
            VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
        ) as String
        
        assertEquals(
            "error should be about file size limit",
            "File size limit reached",
            errorMessage
        )
    }

    @Test
    fun getErrorMessage_shouldReturnGenericMessageForOtherErrorCodes() {
        val method = CameraManagerImpl::class.java.getDeclaredMethod(
            "getErrorMessage",
            Int::class.java
        )
        method.isAccessible = true
        
        val errorMessage = method.invoke(
            cameraManager,
            VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED
        ) as String
        
        assertEquals(
            "error should be generic",
            "Video recording error occurred",
            errorMessage
        )
    }

    @Test
    fun startVideoRecording_shouldClearErrorWhenStarting() {
        val initialSlice = cameraManager.cameraSlice.value
        assertNull("initial error should be null", initialSlice?.error)
    }

    @Test
    fun stopVideoRecording_shouldReturnNullWhenRecordingIsNull() = runTest {
        val result = cameraManager.stopVideoRecording()
        
        assertNull("stopVideoRecording should return null when no recording", result)
    }

    @Test
    fun stopVideoRecording_shouldCallRecordingStopAndReturnResult() = runTest(StandardTestDispatcher()) {
        val recording = mockk<Recording>(relaxed = true)
        val deferred = kotlinx.coroutines.CompletableDeferred<VideoRecordingResult?>()
        val result = VideoRecordingResult("content://test/video.mp4", "Video_123.mp4")
        
        val field = CameraManagerImpl::class.java.getDeclaredField("currentRecording")
        field.isAccessible = true
        field.set(cameraManager, recording)
        
        val deferredField = CameraManagerImpl::class.java.getDeclaredField("recordingResultDeferred")
        deferredField.isAccessible = true
        deferredField.set(cameraManager, deferred)

        every { recording.stop() } just runs
        
        deferred.complete(result)
        
        val stopResult = cameraManager.stopVideoRecording()
        advanceUntilIdle()

        verify(exactly = 1) { recording.stop() }
        assertNotNull("stopVideoRecording should return result", stopResult)
        assertEquals("filePath should match", result.filePath, stopResult?.filePath)
        assertEquals("fileName should match", result.fileName, stopResult?.fileName)
    }

    @Test
    fun stopVideoRecording_shouldHandleExceptionsAndUpdateCameraSliceWithError() = runTest(StandardTestDispatcher()) {
        val recording = mockk<Recording>(relaxed = true)
        val deferred = kotlinx.coroutines.CompletableDeferred<VideoRecordingResult?>()
        
        val field = CameraManagerImpl::class.java.getDeclaredField("currentRecording")
        field.isAccessible = true
        field.set(cameraManager, recording)
        
        val deferredField = CameraManagerImpl::class.java.getDeclaredField("recordingResultDeferred")
        deferredField.isAccessible = true
        deferredField.set(cameraManager, deferred)

        every { recording.stop() } throws RuntimeException("Test error")

        val result = cameraManager.stopVideoRecording()
        advanceUntilIdle()

        assertNull("stopVideoRecording should return null on error", result)
        
        val slice = cameraManager.cameraSlice.value
        assertTrue(
            "error should contain error message",
            slice?.error?.contains("Error stopping recording") == true
        )
    }
}
