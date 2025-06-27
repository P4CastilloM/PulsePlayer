package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Playback_history",
    foreignKeys = [ForeignKey(
        entity = Song::class,
        parentColumns = ["id"],
        childColumns = ["song_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["song_id","id"])]
)
data class Playback_history (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @PrimaryKey val song_id: Int,
    @ColumnInfo(name = "played_at") val played_at: String
)