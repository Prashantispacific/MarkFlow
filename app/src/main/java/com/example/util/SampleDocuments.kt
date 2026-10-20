package com.example.util

data class SampleDoc(
    val title: String,
    val description: String,
    val content: String
)

object SampleDocuments {

    val guides = listOf(
        SampleDoc(
            title = "Markdown Guide.md",
            description = "Complete syntax guide with tables, code, & lists",
            content = """
# Welcome to MarkFlow 🚀

MarkFlow is your lightweight, full-featured **Markdown & Plain Text** editor and viewer for Android.

---

## ⚡ Quick Features
- **Open With Anywhere**: Handles `.md` and `.txt` files directly from Files, WhatsApp, and Gmail.
- **In-Place Saving**: Directly updates the original file without creating duplicates.
- **Viewer & Editor**: Switch effortlessly between rendered Markdown and raw text.
- **Formatting Bar**: Single-tap shortcuts for headers, bold, code, tasks, and lists.
- **Search & Replace**: Jump through matches and replace inline.

---

## 📝 Markdown Syntax Cheat Sheet

### Headings
# Heading 1
## Heading 2
### Heading 3

### Text Styling
- **Bold text** with `**bold**`
- *Italic text* with `*italic*`
- ~~Strikethrough~~ with `~~strikethrough~~`
- Inline `code snippets` with single backticks

### Task List
- [x] Download MarkFlow
- [x] Open external `.md` files
- [ ] Star favorite documents
- [ ] Export to clean HTML

### Blockquote
> "Simplicity is prerequisite for reliability."
> — *Edsger W. Dijkstra*

### Code Block
```kotlin
fun main() {
    val app = "MarkFlow"
    println("Editing markdown on Android made delightful!")
}
```

### Table Example
| Syntax | Description | Shortcut |
| :--- | :--- | :--- |
| `# Title` | Heading 1 | Tap **H1** |
| `**Bold**` | Strong emphasis | Tap **B** |
| `*Italic*` | Emphasis | Tap **I** |
| `- [ ]` | Checklist task | Tap **Task** |

---
*Tip: Tap the eye/pencil icon in the top toolbar to toggle between Viewer and Editor mode!*
            """.trimIndent()
        ),
        SampleDoc(
            title = "Meeting Notes.md",
            description = "Template for team standups and project notes",
            content = """
# Project Standup Notes 📋
**Date:** October 20, 2026  
**Attendees:** Team MarkFlow  

## 🎯 Goals for Sprint
1. Verify Storage Access Framework compatibility
2. Test instant switcher between Editor and Viewer
3. Ensure zero memory leaks on large documents

## 📌 Action Items
- [x] Add Open-With intent filters in AndroidManifest
- [x] Implement Markdown toolbar shortcuts
- [ ] Review document word count statistics
- [ ] Prepare release notes for version 1.0

## 💡 Discussion Topics
- Instant save vs manual save behavior
- Support for offline scratchpad drafts
            """.trimIndent()
        ),
        SampleDoc(
            title = "Task List.md",
            description = "Simple checklist and productivity tracker",
            content = """
# Daily Checklist ✍️

## Priority Tasks
- [x] Review incoming markdown documents
- [ ] Draft article on mobile text editing
- [ ] Clean up temporary downloads
- [ ] Backup important note files

## Ideas Scratchpad
- Export directly to formatted HTML
- Monospace font kerning customization
- Pinning frequently referenced files
            """.trimIndent()
        )
    )
}
