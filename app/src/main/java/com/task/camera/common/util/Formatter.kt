package com.task.camera.common.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatter {
    private const val SECONDS_PER_MINUTE = 60

    fun formatDuration(ms: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % SECONDS_PER_MINUTE
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % SECONDS_PER_MINUTE

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
