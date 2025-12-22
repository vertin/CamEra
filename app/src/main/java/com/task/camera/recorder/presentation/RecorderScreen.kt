package com.task.camera.recorder.presentation

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun RecorderScreen(
    viewModel: RecorderViewModel,
    onNavigateToLibrary: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
            when (action) {
                RecorderAction.NavigateBack -> onNavigateToLibrary()
                is RecorderAction.ShowError -> {
                    snackbarHostState.showSnackbar(action.message)
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        when (val currentState = uiState) {
            is UiState.Initialization -> RecorderInitialization(
                modifier = Modifier.padding(
                    paddingValues
                )
            )

            is UiState.RecorderUiState -> RecorderScreen(
                modifier = Modifier.padding(paddingValues),
                uiState = currentState,
                snackbarHostState = snackbarHostState,
                onBind = viewModel::bind,
                viewModel::startRecording,
                viewModel::stopRecording
            )

            is UiState.NeedPermissions -> CameraPermissionHandlingScreen(
                onPermissionGranted = viewModel::permissionGranted
            )
        }
    }
}

@Composable
private fun RecorderInitialization(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun RecorderInitializationPreview() {
    RecorderInitialization()
}

@SuppressWarnings("LongParameterList")
@Composable
private fun RecorderScreen(
    modifier: Modifier = Modifier,
    uiState: UiState.RecorderUiState,
    snackbarHostState: SnackbarHostState,
    onBind: (LifecycleOwner) -> Unit = { _ -> },
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        uiState.cameraSlice?.let { slice ->
            CameraView(slice, onBind = onBind)

            RecordingButton(
                isRecording = slice.isRecording,
                isSaving = slice.isSaving,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )

            slice.error?.let { error ->
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar(error)
                }
            }
        }
    }
}

@Composable
private fun CameraView(
    cameraSlice: CameraSlice,
    onBind: (LifecycleOwner) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        onBind(lifecycleOwner)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        cameraSlice.surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (cameraSlice.error != null) {
            Text(
                cameraSlice.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                color = Color.Red
            )
        }
    }
}

@Composable
private fun RecordingButton(
    isRecording: Boolean,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {}
) {
    val circleColor by animateColorAsState(
        targetValue = if (isRecording) Color.Red else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "circleColor"
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .clickable(enabled = !isSaving) {
                if (isRecording) {
                    onStopRecording()
                } else {
                    onStartRecording()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )

        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = Color.White
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecorderScreenPreview() {
    RecorderScreen(
        modifier = Modifier,
        uiState = UiState.RecorderUiState(
            cameraSlice = CameraSlice(isSaving = false)
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onStartRecording = {},
        onStopRecording = {}
    )
}
