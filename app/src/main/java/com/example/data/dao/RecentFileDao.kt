package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RecentFile
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY isPinned DESC, lastOpened DESC LIMIT 30")
    fun getRecentFiles(): Flow<List<RecentFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(file: RecentFile): Long

    @Query("SELECT * FROM recent_files WHERE uriString = :uriString LIMIT 1")
    suspend fun findByUri(uriString: String): RecentFile?

    @Query("UPDATE recent_files SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM recent_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}
