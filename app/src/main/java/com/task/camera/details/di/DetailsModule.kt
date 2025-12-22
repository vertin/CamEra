package com.task.camera.details.di

import com.task.camera.common.data.repository.UploadVideoRepositoryImpl
import com.task.camera.common.domain.repository.UploadVideoRepository
import com.task.camera.details.domain.GetVideoByIdUseCase
import com.task.camera.details.domain.ResetVideoStatusUseCase
import com.task.camera.details.domain.SubmitVideoToUploadUseCase
import com.task.camera.details.domain.UploadVideoUseCase
import com.task.camera.details.domain.VideoUploadWorker
import com.task.camera.details.presentation.DetailsViewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playerModule = module {
    singleOf(::GetVideoByIdUseCase)
    viewModelOf(::DetailsViewModel)

    singleOf(::UploadVideoUseCase)
    singleOf<UploadVideoRepository>(::UploadVideoRepositoryImpl)
    singleOf(::SubmitVideoToUploadUseCase)
    singleOf(::ResetVideoStatusUseCase)

    workerOf(::VideoUploadWorker)
}
