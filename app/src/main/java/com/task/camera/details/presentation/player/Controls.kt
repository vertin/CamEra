package com.task.camera.details.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText

@Composable
private fun RowControls(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    additionalSpacer: Float? = null,
    buttons: List<@Composable () -> Unit>,
) {
    Row(modifier, horizontalArrangement, verticalAlignment) {
        buttons.forEachIndexed { index, button ->
            button()
            if (index < buttons.lastIndex && additionalSpacer != null) {
                Spacer(Modifier.weight(additionalSpacer))
            }
        }
    }
}

@Composable
internal fun BoxScope.Controls(player: Player) {
    val buttonModifier = Modifier
        .size(86.dp)
        .background(Color.White.copy(alpha = 0.1f), CircleShape)
    RowControls(
        Modifier
            .fillMaxWidth()
            .align(Alignment.Center),
        buttons =
        listOf(
            {
                PlayPauseButton(
                    player,
                    buttonModifier,
                    tint = Color.White
                )
            },
        ),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        HorizontalLinearProgressIndicator(player, Modifier.fillMaxWidth())
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.4f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PositionAndDurationText(player)
        }
    }
}
