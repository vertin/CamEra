package com.task.camera.recorder.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandlingScreen(
    modifier: Modifier = Modifier,
    onPermissionGranted: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current

    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState.status) {
        if (hasRequestedPermission) {
            permissionRequestCompleted = true
        }
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val status = cameraPermissionState.status) {
                is PermissionStatus.Granted -> {
                    onPermissionGranted.invoke()
                }

                is PermissionStatus.Denied -> {
                    if (permissionRequestCompleted) {
                        if (status.shouldShowRationale) {
                            Text("Camera permission is required to use this feature.")
                            Button(onClick = {
                                cameraPermissionState.launchPermissionRequest()
                                hasRequestedPermission = true
                            }) {
                                Text("Request Camera Permission")
                            }
                        } else {
                            Text("Camera permission denied. Please enable it in the app settings to proceed.")
                            Button(onClick = {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                context.startActivity(intent)
                            }) {
                                Text("Open App Settings")
                            }
                        }
                    } else {
                        Button(onClick = {
                            cameraPermissionState.launchPermissionRequest()
                            hasRequestedPermission = true
                        }) {
                            Text("Request Camera Permission")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraPermissionHandlingScreenPreview() {
    CameraPermissionHandlingScreen(
        onPermissionGranted = {}
    )
}
