package com.task.camera.common.data.mapper

import android.net.Uri
import androidx.media3.common.MediaItem
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MediaItemMapperTest {

    @BeforeEach
    fun setup() {
        mockkStatic(Uri::class)
        mockkStatic(MediaItem::class)
    }

    @org.junit.jupiter.api.AfterEach
    fun tearDown() {
        io.mockk.unmockkAll()
    }

    @Test
    fun `map should convert ExerciseVideo to VideoFile correctly`() {
        // Given
        val mapper = MediaItemMapper()
        val fileUri = "file:///tmp/test_video.mp4"
        val uriMock = mockk<Uri>()
        val mediaItemMock = mockk<MediaItem>()
        every { Uri.parse(fileUri) } returns uriMock
        every { MediaItem.fromUri(uriMock) } returns mediaItemMock
        val video = ExerciseVideo(
            id = 1L,
            filePath = fileUri,
            fileName = "test_video.mp4",
            durationMs = 15000L,
            createdAt = LocalDateTime.now(),
            status = VideoStatus.RECORDED
        )

        // When
        val videoFile = mapper.map(video)

        // Then
        assertEquals("test_video.mp4", videoFile.fileName)
        assertEquals(15000L, videoFile.metadata.durationMs)
        assertEquals(VideoStatus.RECORDED, videoFile.status)
        assertNotNull(videoFile.mediaItem)
        assertEquals(mediaItemMock, videoFile.mediaItem)
    }
}
