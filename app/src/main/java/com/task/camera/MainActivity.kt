package com.task.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.task.camera.details.presentation.DetailsScreen
import com.task.camera.details.presentation.DetailsViewModel
import com.task.camera.library.presentation.LibraryScreen
import com.task.camera.library.presentation.LibraryViewModel
import com.task.camera.navigation.Screen
import com.task.camera.recorder.presentation.RecorderScreen
import com.task.camera.recorder.presentation.RecorderViewModel
import com.task.camera.ui.theme.CamEraTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamEraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CamEraApp()
                }
            }
        }
    }
}

@Composable
fun CamEraApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Library.route
    ) {
        composable(Screen.Library.route) {
            val viewModel: LibraryViewModel = koinViewModel()
            LibraryScreen(
                viewModel = viewModel,
                onVideoClick = { videoId ->
                    navController.navigate(Screen.Player.createRoute(videoId))
                },
                onRecordClick = {
                    navController.navigate(Screen.Recorder.route)
                }
            )
        }

        composable(Screen.Recorder.route) {
            val viewModel: RecorderViewModel = koinViewModel()
            RecorderScreen(
                viewModel = viewModel,
                onNavigateToLibrary = { navController.popBackStack() }
            )
        }

        composable(
            route = "player/{videoId}",
            arguments = listOf(
                navArgument("videoId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween()
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween()
                )
            }
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            val viewModel: DetailsViewModel = koinViewModel(parameters = { parametersOf(videoId) })
            DetailsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
