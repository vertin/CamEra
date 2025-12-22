package com.task.camera.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.library.domain.DeleteVideoUseCase
import com.task.camera.library.domain.GetAllVideosPagedUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class LibraryViewModel(
    getAllVideosPagedUseCase: GetAllVideosPagedUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase,
) : ViewModel() {

    val videos: Flow<PagingData<ExerciseVideo>> = getAllVideosPagedUseCase.launch().cachedIn(viewModelScope)

    private val _action = MutableSharedFlow<LibraryAction>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val actions: SharedFlow<LibraryAction> = _action.asSharedFlow()

    fun onVideoClick(videoId: Long) {
        viewModelScope.launch {
            _action.emit(LibraryAction.OpenDetails(videoId))
        }
    }

    fun recordNewVideo() {
        viewModelScope.launch {
            _action.emit(LibraryAction.RecordNewVideo)
        }
    }

    fun deleteVideo(videoId: Long) {
        viewModelScope.launch {
            deleteVideoUseCase.launch(videoId)
                .take(1)
                .collect { result ->
                    result.onFailure { exception ->
                        _action.emit(
                            LibraryAction.ShowDeleteError(
                                exception.message ?: "Failed to delete video"
                            )
                        )
                    }
                }
        }
    }
}
