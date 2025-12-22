package com.task.camera

import android.app.Application
import androidx.room.Room
import com.task.camera.common.data.local.AppDatabase
import com.task.camera.common.data.local.ExerciseVideoDao
import com.task.camera.common.data.repository.ExerciseVideoRepositoryImpl
import com.task.camera.common.data.service.VideoFileServiceImpl
import com.task.camera.common.data.service.VideoMetadataServiceImpl
import com.task.camera.common.domain.repository.ExerciseVideoRepository
import com.task.camera.common.domain.service.VideoFileService
import com.task.camera.common.domain.service.VideoMetadataService
import com.task.camera.common.util.VideoFileDetector
import com.task.camera.common.util.VideoThumbnailFetcher
import com.task.camera.library.di.libraryModule
import coil.ImageLoader
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TestApplication : Application()

