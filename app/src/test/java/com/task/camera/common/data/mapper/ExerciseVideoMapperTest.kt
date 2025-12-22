package com.task.camera.common.data.mapper

import com.task.camera.common.data.local.ExerciseVideoEntity
import com.task.camera.common.domain.model.ExerciseVideo
import com.task.camera.common.domain.model.VideoStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ExerciseVideoMapperTest {

    @Test
    fun `toDomain should map entity to domain model correctly`() {
        // Given
        val timestamp = 1000L
        val entity = ExerciseVideoEntity(
            id = 1L,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 10000L,
            timestamp = timestamp,
            status = VideoStatus.RECORDED
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(1L, domain.id)
        assertEquals("/path/to/video.mp4", domain.filePath)
        assertEquals("video.mp4", domain.fileName)
        assertEquals(10000L, domain.durationMs)
        assertEquals(VideoStatus.RECORDED, domain.status)
        val expectedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        assertEquals(expectedDateTime, domain.createdAt)
    }

    @Test
    fun `toEntity should map domain model to entity correctly`() {
        // Given
        val createdAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        val domain = ExerciseVideo(
            id = 1L,
            filePath = "/path/to/video.mp4",
            fileName = "video.mp4",
            durationMs = 20000L,
            createdAt = createdAt,
            status = VideoStatus.UPLOADED
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(1L, entity.id)
        assertEquals("/path/to/video.mp4", entity.filePath)
        assertEquals("video.mp4", entity.fileName)
        assertEquals(20000L, entity.durationMs)
        assertEquals(VideoStatus.UPLOADED, entity.status)
        val expectedTimestamp = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expectedTimestamp, entity.timestamp)
    }

    @Test
    fun `toDomain and toEntity should be reversible`() {
        // Given
        val originalEntity = ExerciseVideoEntity(
            id = 5L,
            filePath = "/test/path.mp4",
            fileName = "test.mp4",
            durationMs = 30000L,
            timestamp = 5000L,
            status = VideoStatus.UPLOADING
        )

        // When
        val domain = originalEntity.toDomain()
        val convertedEntity = domain.toEntity()

        // Then
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.filePath, convertedEntity.filePath)
        assertEquals(originalEntity.fileName, convertedEntity.fileName)
        assertEquals(originalEntity.durationMs, convertedEntity.durationMs)
        assertEquals(originalEntity.status, convertedEntity.status)
        assertEquals(originalEntity.timestamp, convertedEntity.timestamp)
    }
}
