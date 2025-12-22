package com.task.camera.details.presentation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.task.camera.R
import com.task.camera.common.domain.model.Metadata
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.util.Formatter
import com.task.camera.details.presentation.player.MediaPlayer
import com.task.camera.ui.theme.CamEraTheme

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is DetailsAction.NavigateBack -> {
                    onNavigateBack()
                }

                is DetailsAction.ShowSnackbar -> snackbarHostState.showSnackbar(action.message)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsState()
        Box(
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding())
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            when (val currentState = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }

                is UiState.Error -> {
                    val errorText = if (currentState.errorMessage.isNotEmpty()) {
                        stringResource(R.string.error_prefix, currentState.errorMessage)
                    } else {
                        stringResource(R.string.error_video_not_found)
                    }
                    Text(text = errorText)
                }

                is UiState.DetailsUiState -> {
                    DetailsContent(
                        uiState = currentState,
                        onUploadClicked = viewModel::uploadVideo
                    )
                }
            }
        }
    }
}

@Composable
internal fun DetailsContent(
    uiState: UiState.DetailsUiState,
    onUploadClicked: () -> Unit,
) {
    val context = LocalContext.current
    val mediaItem = uiState.videoFile.mediaItem
    val previewMode = LocalInspectionMode.current

    val player = remember(mediaItem) {
        try {
            if (!previewMode) {
                initializePlayer(context, mediaItem)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DetailsScreen", "Failed to initialize player", e)
            null
        }
    }

    DisposableEffect(player) {
        onDispose {
            player?.release()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
        ) {
            if (player != null) {
                MediaPlayer(player)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                )
            }
        }
        VideoInfoRow(
            videoFile = uiState.videoFile,
            onUploadClicked = onUploadClicked
        )
    }
}

@Composable
private fun VideoInfoRow(
    modifier: Modifier = Modifier,
    videoFile: VideoFile,
    onUploadClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(videoFile.fileName)
            val durationMs = videoFile.metadata.durationMs
            Text(Formatter.formatDuration(durationMs))
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        UploadButton(
            status = videoFile.status,
            onUploadClicked = onUploadClicked
        )
    }
}

@Composable
private fun UploadButton(
    status: VideoStatus,
    onUploadClicked: () -> Unit
) {
    val buttonColors = when (status) {
        VideoStatus.RECORDED -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )

        VideoStatus.UPLOADING -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )

        VideoStatus.UPLOADED -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    }
    Button(
        onClick = onUploadClicked,
        colors = buttonColors
    ) {
        Text(
            text = when (status) {
                VideoStatus.RECORDED -> stringResource(R.string.video_upload)
                VideoStatus.UPLOADING -> stringResource(R.string.video_uploading)
                VideoStatus.UPLOADED -> stringResource(R.string.video_uploaded)
            }
        )
    }
}

private fun initializePlayer(context: Context, mediaItem: MediaItem): Player =
    ExoPlayer.Builder(context).build().apply {
        setMediaItem(mediaItem)
        prepare()
    }

@Preview
@Composable
private fun DetailsScreen() {
    CamEraTheme {
        Scaffold {
            DetailsContent(
                uiState = UiState.DetailsUiState(
                    VideoFile(
                        mediaItem = MediaItem.Builder().build(),
                        fileName = "preview_video.mp4",
                        metadata = Metadata(durationMs = 1000),
                        status = VideoStatus.RECORDED
                    )
                ),
                {}
            )
        }
    }
}
