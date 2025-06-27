package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Song",
    indices = [Index(value = ["id"], unique = true)]
)

data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "title") val title: String,

    @ColumnInfo(name = "artist_name") val artist_name: String,

    @ColumnInfo(name = "album") val album : String,

    @ColumnInfo(name = "track_number") val track_number: String,

    @ColumnInfo(name = "genre") val genre: String,

    @ColumnInfo(name = "release_year") val release_year: Int,

    @ColumnInfo(name = "cover_image") val cover_image: String?,

    @ColumnInfo(name= "duration_ms") val duration_ms:Int,

    @ColumnInfo(name = "format_duration") val format_duration: String,

    //falta el file path
)