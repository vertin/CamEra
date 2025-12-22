package com.task.camera.common.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.task.camera.common.domain.model.VideoStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseVideoDao {
    @Query("SELECT * FROM exercise_videos ORDER BY timestamp DESC")
    fun getAllVideosPaged(): PagingSource<Int, ExerciseVideoEntity>

    @Query("SELECT * FROM exercise_videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<ExerciseVideoEntity>>

    @Query("SELECT * FROM exercise_videos WHERE id = :id")
    fun getVideoById(id: Long): Flow<ExerciseVideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: ExerciseVideoEntity): Long

    @Query("UPDATE exercise_videos SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: VideoStatus)

    @Query("DELETE FROM exercise_videos WHERE id = :id")
    suspend fun deleteVideo(id: Long)
}
