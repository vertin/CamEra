package com.task.camera.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.camera.common.data.mapper.MediaItemMapper
import com.task.camera.details.domain.GetVideoByIdUseCase
import com.task.camera.details.domain.SubmitVideoToUploadUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
    getVideoByIdUseCase: GetVideoByIdUseCase,
    private val videoMapper: MediaItemMapper,
    private val submitVideoToUploadUseCase: SubmitVideoToUploadUseCase,
    private val videoId: Long
) : ViewModel() {

    private val _action = MutableSharedFlow<DetailsAction>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val actions = _action.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = getVideoByIdUseCase.launch(videoId)
        .flatMapLatest { result ->
            flow {
                result.fold(
                    onSuccess = { video ->
                        emit(
                            UiState.DetailsUiState(
                                videoFile = videoMapper.map(video)
                            )
                        )
                    },
                    onFailure = { _ ->
                        emit(
                            UiState.Error("Cannot fetch the video")
                        )
                        viewModelScope.launch {
                            _action.emit(DetailsAction.NavigateBack)
                        }
                    }
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            UiState.Loading
        )

    fun uploadVideo() {
        viewModelScope.launch {
            submitVideoToUploadUseCase.launch(videoId)
                .fold(
                    onSuccess = { },
                    onFailure = { exception ->
                        _action.emit(
                            DetailsAction.ShowSnackbar(
                                exception.message ?: "Failed to upload video"
                            )
                        )
                    }
                )
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 500L
    }
}
