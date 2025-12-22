package com.task.camera.recorder.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.lifecycle.LifecycleOwner
import com.task.camera.recorder.presentation.CameraSlice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class CameraManagerImpl(
    private val context: Context,
    private val cameraProvider: ProcessCameraProvider,
    private val preview: Preview,
    private val videoCapture: VideoCapture<Recorder>,
    private val executor: Executor
) : CameraManager {

    private val _cameraSlice = MutableStateFlow(CameraSlice())
    override val cameraSlice = _cameraSlice.asStateFlow()

    private var currentRecording: Recording? = null
    private var recordingResultDeferred: CompletableDeferred<VideoRecordingResult?>? = null

    init {
        preview.setSurfaceProvider { request ->
            _cameraSlice.update { it.copy(surfaceRequest = request) }
        }
    }

    override suspend fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture
            )
        } catch (e: IllegalStateException) {
            Log.e("CameraManager", "Camera binding illegal state", e)
        } catch (e: IllegalArgumentException) {
            Log.e("CameraManager", "Invalid camera binding arguments", e)
        } catch (e: RuntimeException) {
            Log.e("CameraManager", "Camera binding failed", e)
        }
    }

    override fun startVideoRecording() {
        _cameraSlice.update { it.copy(error = null) }

        val name = "Video_${System.currentTimeMillis()}.mp4"
        val mediaStoreOutput = createMediaStoreOutput(name)

        recordingResultDeferred = CompletableDeferred()

        try {
            currentRecording = videoCapture.output
                .prepareRecording(context, mediaStoreOutput)
                .start(executor) { event ->
                    handleVideoRecordEvent(event, name)
                }
        } catch (e: IOException) {
            handleRecordingException("Insufficient storage space for video recording", e)
        } catch (e: OutOfMemoryError) {
            handleRecordingException("Insufficient memory for video recording", e)
        } catch (e: Exception) {
            handleRecordingException("Error starting recording: ${e.message ?: "Unknown error"}", e)
        }
    }

    private fun createMediaStoreOutput(fileName: String): MediaStoreOutputOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        return MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setDurationLimitMillis(TimeUnit.MINUTES.toMillis(2))
            .setContentValues(contentValues)
            .build()
    }

    private fun handleVideoRecordEvent(event: VideoRecordEvent, fileName: String) {
        when (event) {
            is VideoRecordEvent.Start -> {
                _cameraSlice.update { it.copy(isRecording = true, error = null) }
            }

            is VideoRecordEvent.Finalize -> {
                handleFinalizeEvent(event, fileName)
            }
        }
    }

    private fun handleFinalizeEvent(event: VideoRecordEvent.Finalize, fileName: String) {
        val deferred = recordingResultDeferred
        recordingResultDeferred = null
        currentRecording = null

        if (event.hasError()) {
            handleRecordingError(event.error, event.cause)
            deferred?.complete(null)
        } else {
            handleRecordingSuccess(event.outputResults.outputUri)
            deferred?.complete(VideoRecordingResult(event.outputResults.outputUri.toString(), fileName))
        }
    }

    private fun handleRecordingError(error: Int, cause: Throwable?) {
        val errorMessage = getErrorMessage(error)
        Log.e("CameraManager", "Video error: $error", cause)
        _cameraSlice.update {
            it.copy(
                isRecording = false,
                isSaving = false,
                error = errorMessage
            )
        }
    }

    private fun handleRecordingSuccess(uri: Uri) {
        val filePath = uri.toString()
        Log.d("CameraManager", "Video saved: $filePath")
        _cameraSlice.update {
            it.copy(
                isRecording = false,
                isSaving = false,
                error = null
            )
        }
    }

    private fun handleRecordingException(errorMessage: String, exception: Throwable) {
        Log.e("CameraManager", errorMessage, exception)
        _cameraSlice.update {
            it.copy(
                isRecording = false,
                isSaving = false,
                error = errorMessage
            )
        }
        recordingResultDeferred?.complete(null)
        recordingResultDeferred = null
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE -> {
                "Insufficient storage space for video recording"
            }
            VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED -> {
                "File size limit reached"
            }
            else -> {
                "Video recording error occurred"
            }
        }
    }

    override suspend fun stopVideoRecording(): VideoRecordingResult? {
        val recording = currentRecording
        val deferred = recordingResultDeferred

        return if (recording == null || deferred == null) {
            null
        } else {
            try {
                _cameraSlice.update {
                    it.copy(
                        isRecording = false,
                        isSaving = true,
                        error = null
                    )
                }
                recording.stop()
                deferred.await()
            } catch (e: Exception) {
                val errorMessage = "Error stopping recording: ${e.message ?: "Unknown error"}"
                Log.e("CameraManager", "Error during recording stop", e)
                _cameraSlice.update {
                    it.copy(
                        isRecording = false,
                        isSaving = false,
                        error = errorMessage
                    )
                }
                null
            }
        }
    }
}
