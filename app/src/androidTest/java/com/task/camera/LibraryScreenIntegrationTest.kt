package com.task.camera

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import coil.ImageLoader
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import com.task.camera.library.domain.DeleteVideoUseCase
import com.task.camera.library.domain.GetAllVideosPagedUseCase
import com.task.camera.library.presentation.LibraryScreen
import com.task.camera.library.presentation.LibraryViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@LargeTest
class LibraryScreenIntegrationTest : KoinTest {
    private lateinit var libraryViewModel: LibraryViewModel

    @get:Rule
    val composeTestRule = createComposeRule()

    @MockK
    private lateinit var deleteVideoUseCase: DeleteVideoUseCase

    @MockK
    private lateinit var getAllVideosPagedUseCase: GetAllVideosPagedUseCase

    @get:Rule
    val koinTestRule = KoinTestRule(
        listOf(
            module {
                viewModel<LibraryViewModel> {
                    LibraryViewModel(
                        getAllVideosPagedUseCase,
                        deleteVideoUseCase
                    )
                }

                single { ImageLoader(get()) }
            }
        ))

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun libraryScreenDisplaysMockedVideos() {

        every { getAllVideosPagedUseCase.launch() } returns flowOf(
            PagingData.from(
                listOf(
                    ExerciseVideo(
                        fileName = "video_1.mp4",
                        durationMs = 10000L,
                        status = VideoStatus.RECORDED,
                        createdAt = LocalDateTime.of(2023, 1, 1, 0, 0),
                        filePath = "/test/path_to_file"
                    )
                )
            )
        )
        libraryViewModel = LibraryViewModel(getAllVideosPagedUseCase, deleteVideoUseCase)

        composeTestRule.setContent {
            LibraryScreen(libraryViewModel, {}, {})
        }

        composeTestRule.onAllNodesWithText("video_1", substring = true)
            .fetchSemanticsNodes().isNotEmpty()

    }

    @Test
    fun libraryScreenDisplaysMoreThan20VideosWithPaging() {
        val mockedVideos = List(30) {
            mockk<ExerciseVideo>(relaxed = true) {
                every { id } returns it.toLong()
                every { fileName } returns "video_$it.mp4"
            }
        }
        val testPager = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            )
        ) {
            mockedVideos.asPagingSourceFactory().invoke()
        }

        every { getAllVideosPagedUseCase.launch() } returns testPager.flow

        libraryViewModel = LibraryViewModel(getAllVideosPagedUseCase, deleteVideoUseCase)

        composeTestRule.setContent {
            LibraryScreen(libraryViewModel, {}, {})
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("video_list")
            .performScrollToIndex(19)

        composeTestRule.waitForIdle()
    }
}
