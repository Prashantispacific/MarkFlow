package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val fileName: String,
    val lastOpened: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0,
    val isPinned: Boolean = false
)
