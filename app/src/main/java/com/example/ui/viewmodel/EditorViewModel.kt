package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.RecentFile
import com.example.data.model.Scratchpad
import com.example.data.repository.RecentFilesRepository
import com.example.data.repository.ScratchpadRepository
import com.example.util.DocumentStats
import com.example.util.FileUtils
import com.example.util.SampleDoc
import com.example.util.SampleDocuments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val fileName: String = "Untitled.md",
    val content: String = "",
    val currentUri: Uri? = null,
    val currentScratchpadId: Long = 0,
    val isPreviewMode: Boolean = true,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val matchIndices: List<Int> = emptyList(),
    val currentMatchIndex: Int = -1,
    val stats: DocumentStats = DocumentStats(0, 0, 0, 0, 0),
    val snackbarMessage: String? = null
)

class EditorViewModel(
    private val recentFilesRepo: RecentFilesRepository,
    private val scratchpadRepo: ScratchpadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val recentFiles: StateFlow<List<RecentFile>> = recentFilesRepo.recentFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scratchpads: StateFlow<List<Scratchpad>> = scratchpadRepo.scratchpads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize with default sample document if no content loaded
        val defaultGuide = SampleDocuments.guides.first()
        _uiState.update {
            it.copy(
                fileName = defaultGuide.title,
                content = defaultGuide.content,
                stats = FileUtils.calculateStats(defaultGuide.content)
            )
        }
    }

    fun handleIncomingUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            FileUtils.takePersistablePermissions(context, uri)
            val name = FileUtils.getFileName(context, uri)
            val size = FileUtils.getFileSize(context, uri)
            val readResult = FileUtils.readTextFromUri(context, uri)

            readResult.onSuccess { text ->
                _uiState.update {
                    it.copy(
                        fileName = name,
                        content = text,
                        currentUri = uri,
                        currentScratchpadId = 0,
                        isPreviewMode = true,
                        isDirty = false,
                        stats = FileUtils.calculateStats(text),
                        snackbarMessage = "Loaded: $name"
                    )
                }
                recentFilesRepo.recordFileOpened(uri.toString(), name, size)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        snackbarMessage = "Failed to open file: ${error.localizedMessage}"
                    )
                }
            }
        }
    }

    fun updateContent(newContent: String) {
        _uiState.update { state ->
            val matches = calculateMatches(newContent, state.searchQuery)
            val newMatchIndex = if (matches.isNotEmpty()) {
                if (state.currentMatchIndex in matches.indices) state.currentMatchIndex else 0
            } else -1

            state.copy(
                content = newContent,
                isDirty = true,
                stats = FileUtils.calculateStats(newContent),
                matchIndices = matches,
                currentMatchIndex = newMatchIndex
            )
        }
    }

    fun togglePreviewMode() {
        _uiState.update { it.copy(isPreviewMode = !it.isPreviewMode) }
    }

    fun setPreviewMode(isPreview: Boolean) {
        _uiState.update { it.copy(isPreviewMode = isPreview) }
    }

    fun createNewDocument() {
        val newContent = "# Untitled Document\n\nStart typing your Markdown here..."
        _uiState.update {
            it.copy(
                fileName = "Untitled.md",
                content = newContent,
                currentUri = null,
                currentScratchpadId = 0,
                isPreviewMode = false,
                isDirty = false,
                stats = FileUtils.calculateStats(newContent),
                snackbarMessage = "Created new document"
            )
        }
    }

    fun loadSample(sample: SampleDoc) {
        _uiState.update {
            it.copy(
                fileName = sample.title,
                content = sample.content,
                currentUri = null,
                currentScratchpadId = 0,
                isPreviewMode = true,
                isDirty = false,
                stats = FileUtils.calculateStats(sample.content),
                snackbarMessage = "Loaded template: ${sample.title}"
            )
        }
    }

    fun loadScratchpad(scratchpad: Scratchpad) {
        _uiState.update {
            it.copy(
                fileName = scratchpad.title,
                content = scratchpad.content,
                currentUri = null,
                currentScratchpadId = scratchpad.id,
                isPreviewMode = false,
                isDirty = false,
                stats = FileUtils.calculateStats(scratchpad.content),
                snackbarMessage = "Loaded note: ${scratchpad.title}"
            )
        }
    }

    fun saveDocument(context: Context) {
        val state = _uiState.value
        val uri = state.currentUri
        val content = state.content

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            if (uri != null) {
                val result = FileUtils.saveTextToUri(context, uri, content)
                result.onSuccess {
                    val size = FileUtils.getFileSize(context, uri)
                    recentFilesRepo.recordFileOpened(uri.toString(), state.fileName, size)
                    _uiState.update {
                        it.copy(
                            isDirty = false,
                            isSaving = false,
                            snackbarMessage = "Saved: ${state.fileName}"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = "Save failed: ${err.localizedMessage}. Use Export or Save As."
                        )
                    }
                }
            } else {
                // If not bound to external URI, save as Scratchpad in Room
                val id = scratchpadRepo.saveScratchpad(
                    id = state.currentScratchpadId,
                    title = state.fileName,
                    content = content
                )
                _uiState.update {
                    it.copy(
                        currentScratchpadId = id,
                        isDirty = false,
                        isSaving = false,
                        snackbarMessage = "Saved to Local Notes: ${state.fileName}"
                    )
                }
            }
        }
    }

    fun insertMarkdown(prefix: String, suffix: String) {
        _uiState.update { state ->
            val updated = state.content + "\n" + prefix + suffix
            state.copy(
                content = updated,
                isDirty = true,
                stats = FileUtils.calculateStats(updated)
            )
        }
    }

    fun setSearchVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                isSearchVisible = visible,
                searchQuery = if (!visible) "" else it.searchQuery,
                matchIndices = if (!visible) emptyList() else it.matchIndices,
                currentMatchIndex = if (!visible) -1 else it.currentMatchIndex
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val matches = calculateMatches(state.content, query)
            state.copy(
                searchQuery = query,
                matchIndices = matches,
                currentMatchIndex = if (matches.isNotEmpty()) 0 else -1
            )
        }
    }

    fun updateReplaceQuery(replace: String) {
        _uiState.update { it.copy(replaceQuery = replace) }
    }

    fun findNextMatch() {
        _uiState.update { state ->
            if (state.matchIndices.isEmpty()) return@update state
            val nextIndex = (state.currentMatchIndex + 1) % state.matchIndices.size
            state.copy(currentMatchIndex = nextIndex)
        }
    }

    fun findPreviousMatch() {
        _uiState.update { state ->
            if (state.matchIndices.isEmpty()) return@update state
            val prevIndex = if (state.currentMatchIndex - 1 < 0) {
                state.matchIndices.size - 1
            } else {
                state.currentMatchIndex - 1
            }
            state.copy(currentMatchIndex = prevIndex)
        }
    }

    fun replaceCurrentMatch() {
        val state = _uiState.value
        val query = state.searchQuery
        val replace = state.replaceQuery
        if (query.isEmpty() || state.matchIndices.isEmpty() || state.currentMatchIndex !in state.matchIndices.indices) {
            return
        }

        val matchOffset = state.matchIndices[state.currentMatchIndex]
        val prefix = state.content.substring(0, matchOffset)
        val suffix = state.content.substring(matchOffset + query.length)
        val updated = prefix + replace + suffix

        val newMatches = calculateMatches(updated, query)
        val newIndex = if (newMatches.isNotEmpty()) {
            state.currentMatchIndex.coerceAtMost(newMatches.size - 1)
        } else -1

        _uiState.update {
            it.copy(
                content = updated,
                isDirty = true,
                stats = FileUtils.calculateStats(updated),
                matchIndices = newMatches,
                currentMatchIndex = newIndex
            )
        }
    }

    fun replaceAllMatches() {
        val state = _uiState.value
        val query = state.searchQuery
        val replace = state.replaceQuery
        if (query.isEmpty()) return

        val count = state.matchIndices.size
        val updated = state.content.replace(query, replace, ignoreCase = true)

        _uiState.update {
            it.copy(
                content = updated,
                isDirty = true,
                stats = FileUtils.calculateStats(updated),
                searchQuery = "",
                matchIndices = emptyList(),
                currentMatchIndex = -1,
                snackbarMessage = "Replaced $count occurrences"
            )
        }
    }

    fun togglePinRecent(id: Long, currentPin: Boolean) {
        viewModelScope.launch {
            recentFilesRepo.togglePin(id, currentPin)
        }
    }

    fun deleteRecent(id: Long) {
        viewModelScope.launch {
            recentFilesRepo.deleteRecent(id)
        }
    }

    fun deleteScratchpad(id: Long) {
        viewModelScope.launch {
            scratchpadRepo.deleteScratchpad(id)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun calculateMatches(text: String, query: String): List<Int> {
        if (query.isBlank() || text.isEmpty()) return emptyList()
        val matches = mutableListOf<Int>()
        var index = text.indexOf(query, 0, ignoreCase = true)
        while (index >= 0) {
            matches.add(index)
            index = text.indexOf(query, index + query.length, ignoreCase = true)
        }
        return matches
    }
}

class EditorViewModelFactory(
    private val recentFilesRepo: RecentFilesRepository,
    private val scratchpadRepo: ScratchpadRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditorViewModel(recentFilesRepo, scratchpadRepo) as T
    }
}
