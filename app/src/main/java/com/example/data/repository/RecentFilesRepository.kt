package com.example.data.repository

import com.example.data.dao.RecentFileDao
import com.example.data.model.RecentFile
import kotlinx.coroutines.flow.Flow

class RecentFilesRepository(private val dao: RecentFileDao) {
    val recentFiles: Flow<List<RecentFile>> = dao.getRecentFiles()

    suspend fun recordFileOpened(uriString: String, fileName: String, sizeBytes: Long) {
        val existing = dao.findByUri(uriString)
        val fileToSave = if (existing != null) {
            existing.copy(
                fileName = fileName,
                lastOpened = System.currentTimeMillis(),
                sizeBytes = if (sizeBytes > 0) sizeBytes else existing.sizeBytes
            )
        } else {
            RecentFile(
                uriString = uriString,
                fileName = fileName,
                lastOpened = System.currentTimeMillis(),
                sizeBytes = sizeBytes
            )
        }
        dao.insertOrUpdate(fileToSave)
    }

    suspend fun togglePin(id: Long, currentPin: Boolean) {
        dao.updatePinned(id, !currentPin)
    }

    suspend fun deleteRecent(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
