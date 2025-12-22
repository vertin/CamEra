package com.task.camera.navigation

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Recorder : Screen("recorder")
    data class Player(val videoId: Long = 0L) : Screen("player/{videoId}") {
        companion object {
            fun createRoute(videoId: Long) = "player/$videoId"
        }
    }
}
