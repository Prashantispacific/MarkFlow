package com.example.data.repository

import com.example.data.dao.ScratchpadDao
import com.example.data.model.Scratchpad
import kotlinx.coroutines.flow.Flow

class ScratchpadRepository(private val dao: ScratchpadDao) {
    val scratchpads: Flow<List<Scratchpad>> = dao.getAllScratchpads()

    suspend fun getScratchpad(id: Long): Scratchpad? = dao.getById(id)

    suspend fun saveScratchpad(id: Long, title: String, content: String): Long {
        return if (id <= 0) {
            dao.insert(
                Scratchpad(
                    title = title.ifBlank { "Untitled Note" },
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.update(
                Scratchpad(
                    id = id,
                    title = title.ifBlank { "Untitled Note" },
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
            )
            id
        }
    }

    suspend fun deleteScratchpad(id: Long) {
        dao.deleteById(id)
    }
}
