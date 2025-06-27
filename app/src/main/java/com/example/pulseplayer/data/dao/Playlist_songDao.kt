package com.example.pulseplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pulseplayer.data.entity.Playlist_song
import kotlinx.coroutines.flow.Flow

@Dao
interface Playlist_songDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlistSong: Playlist_song)

    @Update
    suspend fun update(playlistSong: Playlist_song)

    @Delete
    suspend fun delete(playlistSong: Playlist_song)

    @Query("SELECT * FROM Playlist_song")
    fun getAll():Flow<List<Playlist_song>>

    @Query("SELECT * FROM Playlist_song WHERE song_id =:song_id")
    fun getBySongId(song_id:Int): Flow<Playlist_song>

    @Query("SELECT * FROM Playlist_song WHERE playlist_id =:playlist_id")
    fun getBy
}