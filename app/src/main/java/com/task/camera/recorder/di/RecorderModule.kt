package com.task.camera.recorder.di

import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import com.task.camera.recorder.camera.CameraManager
import com.task.camera.recorder.camera.CameraManagerImpl
import com.task.camera.recorder.presentation.RecorderViewModel
import com.task.camera.recorder.presentation.domain.SaveVideoFileUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.concurrent.Executor

val recorderModule = module {
    viewModelOf(::RecorderViewModel)

    single {
        ProcessCameraProvider.getInstance(get()).get()
            ?: error("Failed to get ProcessCameraProvider")
    }

    factory {
        Preview.Builder().build()
    }

    factory {
        VideoCapture.withOutput(
            Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build()
        )
    }

    factory<Executor> {
        ContextCompat.getMainExecutor(get())
    }

    factory<CameraManager> {
        CameraManagerImpl(
            context = get(),
            cameraProvider = get(),
            preview = get(),
            videoCapture = get(),
            executor = get()
        )
    }

    singleOf(::SaveVideoFileUseCase)
}
