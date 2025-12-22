package com.task.camera.common.di

import androidx.work.WorkManager
import coil.ImageLoader
import com.task.camera.common.data.local.AppDatabase
import com.task.camera.common.data.local.ExerciseVideoDao
import com.task.camera.common.data.mapper.MediaItemMapper
import com.task.camera.common.data.repository.ExerciseVideoRepositoryImpl
import com.task.camera.common.data.service.VideoFileServiceImpl
import com.task.camera.common.data.service.VideoMetadataServiceImpl
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoFileService
import com.task.camera.common.domain.service.VideoMetadataService
import com.task.camera.common.util.VideoThumbnailFetcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

@SuppressWarnings("InjectDispatcher")
val appModule = module {
    single<AppDatabase> { AppDatabase.create(androidContext()) }
    single<ExerciseVideoDao> { get<AppDatabase>().exerciseVideoDao() }
    single { WorkManager.getInstance(get()) }

    single<CoroutineDispatcher>(named("IODispatcher")) { Dispatchers.IO }

    single<VideoMetadataService> {
        VideoMetadataServiceImpl(
            context = androidContext(),
            ioDispatcher = get(named("IODispatcher"))
        )
    }
    single<VideoFileService> { VideoFileServiceImpl(androidContext()) }
    singleOf(::MediaItemMapper)

    single<ExerciseVideoRepository> { ExerciseVideoRepositoryImpl(get()) }

    single<ImageLoader> {
        ImageLoader.Builder(androidContext())
            .components {
                add(VideoThumbnailFetcher.Factory())
            }
            .build()
    }
}
