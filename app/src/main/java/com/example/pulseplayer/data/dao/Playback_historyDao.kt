package com.example.pulseplayer.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pulseplayer.data.entity.Playback_history
import kotlinx.coroutines.flow.Flow

interface Playback_historyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playbackHistory: Playback_history)

    @Update
    suspend fun update(playbackHistory: Playback_history)

    @Delete
    suspend fun delete(playbackHistory: Playback_history)

    @Query("SELECT * FROM Playback_history")
    fun getAll():Flow<List<Playback_history>>

    @Query("SELECT * FROM Playback_history WHERE id =:id")
    fun getById(id:Int): Flow<Playback_history>
}