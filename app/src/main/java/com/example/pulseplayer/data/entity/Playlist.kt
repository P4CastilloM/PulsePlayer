package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Playlist",
    indices = [Index(value = ["id"], unique = true)]
)
data class Playlist (
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val created_at: String
)