# MarkFlow - Modern Markdown & Text Editor and Viewer

MarkFlow is a lightweight, responsive Markdown and plain text editor and viewer for Android designed to seamlessly handle external `.md`, `.markdown`, and `.txt` files through system "Open with" dialogs while providing rich in-app editing, rendered previews, and document management.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX decisions were confirmed through Phase 1 clarifications:
> - **View & Edit Layout**: Instant toggle between rendered Markdown Viewer and raw Monospace Editor, with a floating/top-bar quick switcher.
> - **Document Organization**: Integrated Recent Files manager, quick Scratchpad with auto-save, and built-in interactive sample documents (Cheat Sheet, Tasks list, Release notes).
> - **Productivity Suite**: Full Markdown formatting symbol bar, real-time Search & Replace with match navigation and match count, Document Statistics (word count, char count, estimated reading time), and Document Export to formatted HTML and plain text.
> - **Data Persistence**: Uses Android Storage Access Framework (SAF) for direct in-place updates to opened files on internal storage, SD card, or cloud providers, paired with Room Database for recent files history, bookmarks, and scratchpads.

---

### 1. Overview & Core Concept

- **What It Does**: MarkFlow acts as both an external file handler (opening `.md`, `.markdown`, and `.txt` files from file explorers, messaging apps, and email clients) and an independent writing environment with live rendered previews, quick syntax shortcuts, search/replace, and export tools.
- **Target Audience / Persona**: Developers, technical writers, students, and note-takers who need a fast, distraction-free tool to inspect, edit, and share Markdown documents on Android without heavy cloud lock-in or unnecessary bloat.
- **Key Value**: Instant startup, true in-place file editing without creating orphaned copies, rich CommonMark/GFM rendering (tables, checklists, strikethrough, headers), and ergonomic mobile typing tools.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Open With Flow**: User taps a `.md` or `.txt` file in any third-party app (e.g., Files, WhatsApp, Drive). Android presents MarkFlow in the "Open with" sheet. The document loads immediately in polished **Viewer Mode**. The user can copy content, inspect stats, switch to **Editor Mode** with one tap, make changes, and hit Save to write directly back to the original source URI.
2. **In-App Library & Scratchpad Flow**: Launching the app directly opens the Library dashboard featuring quick stats, pinned scratchpads, recently opened files with timestamps, and starter Markdown guides. Users can create a new blank file, open any file via the system picker (`Open Document`), or edit scratchpad notes.
3. **Editing & Formatting Flow**: In Editor mode, a dedicated dockable Markdown Toolbar provides single-tap formatting for `# H1`, `## H2`, `**Bold**`, `*Italic*`, ````Code````, `- [ ] Task`, `> Quote`, and `[Link]()`.
4. **Search & Replace Flow**: Users tap the search icon to reveal an inline search/replace bar with next/prev match buttons, match counters, and one-tap replace/replace-all.
5. **Export & Share Flow**: Users can copy raw text, share via Android Sharesheet, or export rendered content to standalone HTML with clean CSS styling.

#### Visual Identity & Theme
- **Aesthetic Direction**: Clean editorial minimalism inspired by modern typography and markdown notebooks (Obsidian/Bear style), featuring high-contrast readability and warm surface elevation.
- **Color Palette**:
  - Primary: Deep Indigo / Slate Blue (`#3B5998` / `#6366F1`)
  - Accent: Coral Amber (`#F59E0B`) for highlights, checklists, and search matches
  - Surface & Background: Warm neutral light (`#FBFBFD`) and dark obsidian (`#121316`)
  - Code & Monospace blocks: Subtle elevated tint (`#F1F3F9` / `#1E2028`) with distinct syntax contrast
- **Typography & Hierarchy**:
  - Display & Headings: Clean sans-serif with distinct weights for H1–H6
  - Editor text: Crisp monospace font family with comfortable line spacing and character kerning
  - Rendered view: High-readability proportional styling with indented blockquotes and padded tables
- **Interactive Feedback & Motion**:
  - Smooth spring transitions between Viewer and Editor modes
  - Visual pulse indicators for search match navigation
  - Toast and snackbar confirmations on file save, copy, and export

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Markdown Rendering Engine**
  - *Chosen Approach*: Native Compose-compatible Markdown renderer supporting GitHub Flavored Markdown (GFM), including headers, emphasis, tables, task lists, and code blocks.
  - *Why*: Delivers snappy native rendering without requiring heavy WebViews, maintaining smooth 60fps scrolling and selectable text copy.
  - *Alternatives Considered*: WebView with JavaScript libraries (higher memory consumption, sluggish touch selection).

- **Decision 2: In-Place Storage Access Framework (SAF) vs Local Mirroring**
  - *Chosen Approach*: True in-place read/write via `contentResolver.openInputStream(uri)` and `contentResolver.openOutputStream(uri, "wt")` with persistable permission persistence when available.
  - *Why*: Prevents creating duplicate files across storage; edits made by the user are immediately reflected in their original folder or cloud-synced directory.
  - *Fallback*: If a URI is read-only, the app clearly notifies the user and offers "Save As" / "Export Copy".

- **Decision 3: Local Storage for Recent Files & Scratchpad**
  - *Chosen Approach*: Room Database storing file metadata (URI, file name, size, last opened timestamp, bookmark status) and local quick-notes.
  - *Why*: Fast asynchronous reactive queries via Kotlin `Flow`, lightweight footprint, and zero cloud authentication required.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Android OS & External Apps                      │
│   (Files / Drive / Mail Intent Filter: ACTION_VIEW / ACTION_EDIT)      │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ SAF URI
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│              (Intent Handler & Edge-to-Edge Host UI)                   │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            EditorViewModel                             │
│       - Current File State (URI, Title, Dirty Flag, Content)          │
│       - View Mode (Viewer vs Editor vs Raw)                            │
│       - Search & Replace Engine (Query, Matches, Active Index)         │
│       - Document Metrics (Word Count, Char Count, Read Time)           │
└───────────────┬──────────────────────────────────┬─────────────────────┘
                │                                  │
    Direct I/O  ▼                     Room Cache   ▼
┌───────────────────────────────┐  ┌─────────────────────────────────────┐
│       ContentResolver         │  │            AppDatabase              │
│  - openInputStream(uri)       │  │  - RecentFileDao (URI & metadata)   │
│  - openOutputStream(uri, "wt")│  │  - ScratchpadDao (Local documents)  │
└───────────────────────────────┘  └─────────────────────────────────────┘
                │                                  │
                └───────────────┬──────────────────┘
                                │ StateFlow
                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            UI Layer (M3)                               │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ TopAppBar: Title, Mode Toggle, Search, Copy, Save, Export, Menu  │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Main Content Area:                                               │  │
│  │   • ViewerMode: Rendered Markdown (Tables, Tasks, Code, Lists)   │  │
│  │   • EditorMode: Monospace Editor + Line Numbers + Search Match   │  │
│  │   • LibrarySheet: Recent Files, Scratchpad Notes, Sample Docs    │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Search & Replace Bar (Collapsible with count & prev/next)        │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Markdown Quick Formatting Bar (H1, H2, B, I, Code, List, Task)   │  │
│  ├──────────────────────────────────────────────────────────────────┤  │
│  │ Document Status Bar (Word count, Characters, Saved status)       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

#### Component & State Details:
1. **`EditorViewModel`**:
   - `uiState: StateFlow<EditorUiState>` containing current document text, file URI, file title, modified status (`isDirty`), mode (`VIEWER` / `EDITOR`), search state (`searchQuery`, `replaceQuery`, `matchIndices`, `currentMatchIndex`), and stats (`words`, `chars`, `readMinutes`).
   - Actions: `loadUri(Uri)`, `newDocument()`, `updateContent(String)`, `saveCurrentDocument()`, `insertMarkdown(prefix, suffix)`, `search(String)`, `replaceNext()`, `replaceAll()`, `exportHtml(Context)`.
2. **`RecentFilesRepository` & Room DAO**:
   - Tracks recently opened files with URI string, file name, timestamp, and byte size.
   - Manages quick scratchpad notes for instant thoughts without requiring an external file.
3. **Intent Filters**:
   - Configured for `ACTION_VIEW` and `ACTION_EDIT` covering MIME types `text/markdown`, `text/x-markdown`, `text/plain`, `application/octet-stream` (with path patterns `.*\\.md`, `.*\\.markdown`, `.*\\.txt`).
