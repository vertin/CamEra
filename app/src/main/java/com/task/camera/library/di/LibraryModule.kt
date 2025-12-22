package com.task.camera.library.di

import com.task.camera.library.domain.DeleteVideoUseCase
import com.task.camera.library.domain.GetAllVideosPagedUseCase
import com.task.camera.library.presentation.LibraryViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryModule = module {
    singleOf(::GetAllVideosPagedUseCase)
    singleOf(::DeleteVideoUseCase)
    viewModelOf(::LibraryViewModel)
}
