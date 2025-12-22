package com.task.camera.library.presentation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import coil.compose.AsyncImage
import com.task.camera.R
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.common.util.Formatter
import com.task.camera.common.util.VideoThumbnailFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.koinInject
import java.time.LocalDateTime
import kotlin.math.roundToInt

private const val MAX_SWIPE_DISTANCE = 500f

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onVideoClick: (Long) -> Unit,
    onRecordClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val imageLoader: ImageLoader = koinInject()

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is LibraryAction.RecordNewVideo -> {
                    onRecordClick()
                }

                is LibraryAction.OpenDetails -> {
                    onVideoClick(action.videoId)
                }

                is LibraryAction.ShowDeleteError -> {
                    snackbarHostState.showSnackbar(action.message)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::recordNewVideo,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_24px),
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        LibraryScreenListContent(
            videosFlow = viewModel.videos,
            modifier = Modifier.padding(paddingValues),
            onVideoClick = viewModel::onVideoClick,
            onDeleteVideo = viewModel::deleteVideo,
            imageLoader = imageLoader
        )
    }
}

@Composable
private fun LibraryScreenListContent(
    videosFlow: Flow<PagingData<ExerciseVideo>>,
    modifier: Modifier = Modifier,
    onVideoClick: (Long) -> Unit,
    onDeleteVideo: (Long) -> Unit,
    imageLoader: ImageLoader
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = TopCenter
    ) {
        val videos = videosFlow.collectAsLazyPagingItems()
        val isInitialLoading =
            videos.loadState.refresh is LoadState.Loading && videos.itemCount == 0
        when {
            isInitialLoading -> {
                LibraryScreenLoadingState()
            }

            videos.loadState.refresh is LoadState.Error -> {
                LibraryScreenErrorState(videos.loadState.refresh as LoadState.Error)
            }

            videos.itemCount == 0 && videos.loadState.refresh !is LoadState.Loading -> {
                LibraryScreenEmptyState()
            }

            else -> {
                LibraryScreenVideoList(
                    videos = videos,
                    onVideoClick = onVideoClick,
                    onDeleteVideo = onDeleteVideo,
                    imageLoader = imageLoader
                )
            }
        }
    }
}

@Composable
private fun LibraryScreenLoadingState() {
    CircularProgressIndicator()
}

@Composable
private fun LibraryScreenErrorState(error: LoadState.Error) {
    Text(
        text = "Error: ${error.error.message ?: "Unknown error"}",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun LibraryScreenEmptyState() {
    Text(
        text = "No videos recorded yet",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun LibraryScreenVideoList(
    videos: LazyPagingItems<ExerciseVideo>,
    onVideoClick: (Long) -> Unit,
    onDeleteVideo: (Long) -> Unit,
    imageLoader: ImageLoader
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTag = "video_list" }
    ) {
        items(
            count = videos.itemCount,
            key = videos.itemKey { it.id }
        ) { position ->
            val video = videos[position]
            if (video != null) {
                SwipeToDeleteVideoItem(
                    video = video,
                    onVideoClick = { onVideoClick(video.id) },
                    onDelete = { onDeleteVideo(video.id) },
                    imageLoader = imageLoader
                )
            }
        }

        item {
            LibraryScreenPagingFooter(videos.loadState.append)
        }
    }
}

@Composable
private fun LibraryScreenPagingFooter(appendState: LoadState) {
    when {
        appendState is LoadState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics { testTag = "paging_loading_indicator" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        appendState is LoadState.Error -> {
            Text(
                text = "Error loading more videos",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SwipeToDeleteVideoItem(
    video: ExerciseVideo,
    onVideoClick: () -> Unit,
    onDelete: () -> Unit,
    imageLoader: ImageLoader
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        label = "swipe_offset"
    )

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = offsetX < 0,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red)
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_sweep_24px),
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -200f) {
                                onDelete()
                            } else {
                                offsetX = 0f
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceIn(-MAX_SWIPE_DISTANCE, 0f)
                    }
                }
                .background(MaterialTheme.colorScheme.surface)
        ) {
            VideoItem(
                video = video,
                onClick = onVideoClick,
                imageLoader = imageLoader
            )
        }
    }
}

@Composable
private fun VideoItem(
    video: ExerciseVideo,
    onClick: () -> Unit,
    imageLoader: ImageLoader
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = video.filePath,
                    contentDescription = "Video thumbnail",
                    imageLoader = imageLoader,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = video.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = Formatter.formatDuration(video.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Text(
                    text = video.status.name,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun createPreviewImageLoader(context: Context): ImageLoader {
    return remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoThumbnailFetcher.Factory())
            }
            .build()
    }
}

@Composable
private fun createPreviewVideos(): Flow<PagingData<ExerciseVideo>> {
    return flowOf(
        PagingData.from(
            listOf(
                ExerciseVideo(
                    fileName = "video",
                    filePath = "",
                    durationMs = 10000L,
                    createdAt = LocalDateTime.now(),
                    status = VideoStatus.RECORDED
                ),
                ExerciseVideo(
                    fileName = "video 2",
                    filePath = "",
                    durationMs = 20000L,
                    createdAt = LocalDateTime.now(),
                    status = VideoStatus.UPLOADING
                ),
                ExerciseVideo(
                    fileName = "video 3",
                    filePath = "",
                    durationMs = 30000L,
                    createdAt = LocalDateTime.now(),
                    status = VideoStatus.UPLOADED
                ),
            ),
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(false),
                append = LoadState.NotLoading(false),
                prepend = LoadState.NotLoading(false),
            ),

        ),
    )
}

@Preview
@Composable
private fun VideoItemPreview() {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoThumbnailFetcher.Factory())
            }
            .build()
    }
    VideoItem(
        video = ExerciseVideo(
            id = 1L,
            filePath = "/path/to/video.mp4",
            fileName = "Sample Video Recording",
            durationMs = 10000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        ),
        onClick = {},
        imageLoader = imageLoader
    )
}

@Preview
@Composable
private fun LibraryScreenLoadingPreview() {
    Scaffold { paddingValues ->
        val context = LocalContext.current
        val imageLoader = createPreviewImageLoader(context)
        LibraryScreenListContent(
            modifier = Modifier.padding(paddingValues),
            videosFlow = flowOf(PagingData.empty()),
            onVideoClick = {},
            onDeleteVideo = {},
            imageLoader = imageLoader
        )
    }
}

@Preview
@Composable
private fun LibraryScreenPreview() {
    Scaffold { paddingValues ->
        val context = LocalContext.current
        val imageLoader = createPreviewImageLoader(context)
        val videosFlow = createPreviewVideos()

        LibraryScreenListContent(
            videosFlow = videosFlow,
            modifier = Modifier.padding(paddingValues),
            onVideoClick = {},
            onDeleteVideo = {},
            imageLoader = imageLoader
        )
    }
}
