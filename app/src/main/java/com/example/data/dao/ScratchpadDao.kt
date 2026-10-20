package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Scratchpad
import kotlinx.coroutines.flow.Flow

@Dao
interface ScratchpadDao {
    @Query("SELECT * FROM scratchpads ORDER BY updatedAt DESC")
    fun getAllScratchpads(): Flow<List<Scratchpad>>

    @Query("SELECT * FROM scratchpads WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Scratchpad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scratchpad: Scratchpad): Long

    @Update
    suspend fun update(scratchpad: Scratchpad)

    @Query("DELETE FROM scratchpads WHERE id = :id")
    suspend fun deleteById(id: Long)
}
