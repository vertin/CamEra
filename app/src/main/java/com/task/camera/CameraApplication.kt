package com.task.camera

import android.app.Application
import com.task.camera.common.di.appModule
import com.task.camera.details.di.playerModule
import com.task.camera.library.di.libraryModule
import com.task.camera.recorder.di.recorderModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class CameraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CameraApplication)
            workManagerFactory()
            modules(
                appModule,
                libraryModule,
                recorderModule,
                playerModule
            )
        }
    }
}
