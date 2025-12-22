package com.task.camera.common.data.mapper

import com.task.camera.common.data.local.ExerciseVideoEntity
import com.task.camera.common.domain.model.ExerciseVideo
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ExerciseVideoEntity.toDomain(): ExerciseVideo {
    return ExerciseVideo(
        id = id,
        filePath = filePath,
        fileName = fileName,
        durationMs = durationMs,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()),
        status = status
    )
}

fun ExerciseVideo.toEntity(): ExerciseVideoEntity {
    return ExerciseVideoEntity(
        id = id,
        filePath = filePath,
        fileName = fileName,
        durationMs = durationMs,
        timestamp = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        status = status
    )
}
