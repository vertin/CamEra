package com.task.camera.details.presentation.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState

/** PlayerView equivalent - content frame with aspect ratio plus centre and bottom controls */
@Composable
internal fun MediaPlayer(
    player: Player,
) {
    var showControls by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        ContentFrame(
            player = player,
            modifier = Modifier
                .fillMaxSize()
                .noRippleClickable { showControls = !showControls },
        )

        if (showControls) {
            Controls(player)
        }
    }
}

@Composable
fun ContentFrame(
    player: Player?,
    modifier: Modifier = Modifier,
    config: ContentFrameConfig = ContentFrameConfig()
) {
    val presentationState = rememberPresentationState(player, config.keepContentOnReset)
    val scaledModifier =
        modifier.resizeWithContentScale(
            contentScale = config.contentScale,
            sourceSizeDp = presentationState.videoSizeDp
        )

    PlayerSurface(
        player,
        modifier = scaledModifier,
        surfaceType = config.surfaceType
    )

    if (presentationState.coverSurface) {
        config.shutter()
    }
}
