package com.task.camera.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.task.camera.common.domain.model.VideoStatus

@Entity(tableName = "exercise_videos")
data class ExerciseVideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val durationMs: Long,
    val timestamp: Long,
    val status: VideoStatus
)
