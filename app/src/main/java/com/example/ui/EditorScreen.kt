package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DocumentStatsDialog
import com.example.ui.components.EditorContent
import com.example.ui.components.LibrarySheet
import com.example.ui.components.MarkdownToolbar
import com.example.ui.components.MarkdownViewer
import com.example.ui.components.SearchBar
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.viewmodel.EditorViewModel
import com.example.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    val scratchpads by viewModel.scratchpads.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showLibrarySheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleIncomingUri(context, uri)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val hairlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Apple/Google style elevated header with hair-line border and segmented mode switcher
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = hairlineColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // Top Navigation and Actions Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showLibrarySheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("library_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                                contentDescription = "Document Library",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Document Title & Saved Status Badge
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.fileName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(Modifier.width(6.dp))

                                // Status Pill
                                if (uiState.isDirty) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Edited",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Saved",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }

                        // Header Actions (Search, Copy, Save, Menu)
                        IconButton(
                            onClick = { viewModel.setSearchVisible(!uiState.isSearchVisible) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("action_search_toggle")
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                FileUtils.copyToClipboard(context, uiState.fileName, uiState.content)
                                Toast.makeText(context, "Copied raw content to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("action_copy_content")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Content",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.saveDocument(context) },
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("action_save_document")
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = "Save Document",
                                    tint = if (uiState.isDirty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("action_overflow_menu")
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Open File...", fontFamily = PlusJakartaSansFamily) },
                                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        openDocumentLauncher.launch(
                                            arrayOf("text/*", "application/octet-stream", "*/*")
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New Document", fontFamily = PlusJakartaSansFamily) },
                                    leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.createNewDocument()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Text", fontFamily = PlusJakartaSansFamily) },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        FileUtils.shareText(context, uiState.content, "Share ${uiState.fileName}")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export to HTML", fontFamily = PlusJakartaSansFamily) },
                                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        FileUtils.exportToHtml(context, uiState.content, uiState.fileName)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Document Statistics", fontFamily = PlusJakartaSansFamily) },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        showStatsDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Apple/Google Style Segmented Capsule Switcher (View vs Edit)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Viewer Tab Pill
                        val isView = uiState.isPreviewMode
                        val viewBg by animateColorAsState(
                            targetValue = if (isView) MaterialTheme.colorScheme.surface else Color.Transparent,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "viewBg"
                        )
                        val viewColor by animateColorAsState(
                            targetValue = if (isView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "viewColor"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(viewBg)
                                .clickable { viewModel.setPreviewMode(true) }
                                .testTag("segmented_view_tab")
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = viewColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Preview",
                                fontSize = 13.sp,
                                fontWeight = if (isView) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = PlusJakartaSansFamily,
                                color = viewColor
                            )
                        }

                        // Editor Tab Pill
                        val isEdit = !uiState.isPreviewMode
                        val editBg by animateColorAsState(
                            targetValue = if (isEdit) MaterialTheme.colorScheme.surface else Color.Transparent,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "editBg"
                        )
                        val editColor by animateColorAsState(
                            targetValue = if (isEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "editColor"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(editBg)
                                .clickable { viewModel.setPreviewMode(false) }
                                .testTag("segmented_edit_tab")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = editColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Editor",
                                fontSize = 13.sp,
                                fontWeight = if (isEdit) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = PlusJakartaSansFamily,
                                color = editColor
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                if (!uiState.isPreviewMode) {
                    // Floating Markdown Toolbar Dock
                    MarkdownToolbar(
                        onInsert = { prefix, suffix ->
                            viewModel.insertMarkdown(prefix, suffix)
                        }
                    )
                } else {
                    // Apple/Google Style Floating Document Stats Capsule
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            shadowElevation = 6.dp,
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${uiState.stats.words} words • ~${uiState.stats.readTimeMinutes} min read",
                                        fontSize = 12.5.sp,
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { viewModel.setPreviewMode(false) }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Edit Text",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = PlusJakartaSansFamily,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search and Replace Floating Panel
            SearchBar(
                isVisible = uiState.isSearchVisible,
                searchQuery = uiState.searchQuery,
                replaceQuery = uiState.replaceQuery,
                matchCount = uiState.matchIndices.size,
                currentMatchIndex = uiState.currentMatchIndex,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                onReplaceChange = { viewModel.updateReplaceQuery(it) },
                onNextMatch = { viewModel.findNextMatch() },
                onPreviousMatch = { viewModel.findPreviousMatch() },
                onReplace = { viewModel.replaceCurrentMatch() },
                onReplaceAll = { viewModel.replaceAllMatches() },
                onClose = { viewModel.setSearchVisible(false) }
            )

            // Animated Smooth Transition between Viewer and Monospace Editor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = uiState.isPreviewMode,
                    transitionSpec = {
                        fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                    },
                    label = "EditorViewerSmoothTransition"
                ) { isPreview ->
                    if (isPreview) {
                        MarkdownViewer(
                            markdown = uiState.content,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EditorContent(
                            content = uiState.content,
                            onContentChange = { viewModel.updateContent(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showStatsDialog) {
        DocumentStatsDialog(
            fileName = uiState.fileName,
            stats = uiState.stats,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showLibrarySheet) {
        LibrarySheet(
            recentFiles = recentFiles,
            scratchpads = scratchpads,
            onOpenStoragePicker = {
                openDocumentLauncher.launch(
                    arrayOf("text/*", "application/octet-stream", "*/*")
                )
            },
            onSelectRecent = { uri ->
                viewModel.handleIncomingUri(context, uri)
            },
            onTogglePinRecent = { id, pin ->
                viewModel.togglePinRecent(id, pin)
            },
            onDeleteRecent = { id ->
                viewModel.deleteRecent(id)
            },
            onSelectScratchpad = { pad ->
                viewModel.loadScratchpad(pad)
            },
            onDeleteScratchpad = { id ->
                viewModel.deleteScratchpad(id)
            },
            onSelectSample = { sample ->
                viewModel.loadSample(sample)
            },
            onNewScratchpad = {
                viewModel.createNewDocument()
            },
            onDismiss = { showLibrarySheet = false }
        )
    }
}
