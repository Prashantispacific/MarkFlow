package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

data class DocumentStats(
    val words: Int,
    val characters: Int,
    val charactersNoSpaces: Int,
    val lines: Int,
    val readTimeMinutes: Int
)

object FileUtils {

    fun readTextFromUri(context: Context, uri: Uri): Result<String> {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalStateException("Unable to open input stream for URI: $uri")
        }
    }

    fun saveTextToUri(context: Context, uri: Uri, content: String): Result<Unit> {
        return runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            } ?: throw IllegalStateException("Unable to open output stream for writing: $uri")
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "Untitled.md"
        val scheme = uri.scheme
        if (scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            val displayName = cursor.getString(index)
                            if (!displayName.isNullOrBlank()) {
                                name = displayName
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to last path segment
                uri.lastPathSegment?.let { segment ->
                    name = segment.substringAfterLast('/')
                }
            }
        } else if (scheme == "file") {
            uri.lastPathSegment?.let { segment ->
                name = segment.substringAfterLast('/')
            }
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (index != -1) {
                        size = cursor.getLong(index)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return size
    }

    fun takePersistablePermissions(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: Exception) {
            // Some providers don't support persistable flags; safely ignored
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun shareText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun calculateStats(text: String): DocumentStats {
        if (text.isBlank()) {
            return DocumentStats(0, 0, 0, 0, 0)
        }
        val lines = text.lines().size
        val characters = text.length
        val charactersNoSpaces = text.filter { !it.isWhitespace() }.length
        val words = text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
        val readTimeMinutes = kotlin.math.max(1, (words + 199) / 200)
        return DocumentStats(words, characters, charactersNoSpaces, lines, readTimeMinutes)
    }

    fun exportToHtml(context: Context, markdownText: String, title: String): Result<String> {
        return runCatching {
            // Render basic HTML template with standard styling
            val escapedTitle = title.replace("<", "&lt;").replace(">", "&gt;")
            val htmlContent = buildHtmlDocument(escapedTitle, markdownText)
            
            // Share HTML file or text
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, "$escapedTitle.html")
                putExtra(Intent.EXTRA_TEXT, htmlContent)
                type = "text/html"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export HTML Document"))
            htmlContent
        }
    }

    private fun buildHtmlDocument(title: String, markdown: String): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$title</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        line-height: 1.6;
                        color: #1e293b;
                        background: #f8fafc;
                        margin: 0;
                        padding: 24px;
                    }
                    .container {
                        max-width: 800px;
                        margin: 0 auto;
                        background: #ffffff;
                        padding: 32px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
                    }
                    h1, h2, h3, h4, h5, h6 { color: #0f172a; margin-top: 1.5em; }
                    h1 { border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; }
                    code {
                        background: #f1f5f9;
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-family: monospace;
                        font-size: 0.9em;
                    }
                    pre {
                        background: #1e293b;
                        color: #f8fafc;
                        padding: 16px;
                        border-radius: 8px;
                        overflow-x: auto;
                    }
                    pre code { background: none; color: inherit; }
                    blockquote {
                        border-left: 4px solid #6366f1;
                        margin-left: 0;
                        padding-left: 16px;
                        color: #475569;
                    }
                    table {
                        border-collapse: collapse;
                        width: 100%;
                        margin: 16px 0;
                    }
                    th, td {
                        border: 1px solid #cbd5e1;
                        padding: 10px 14px;
                        text-align: left;
                    }
                    th { background: #f1f5f9; }
                    a { color: #4f46e5; text-decoration: none; }
                    a:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                <div class="container">
                    <pre style="white-space: pre-wrap; font-family: inherit; background: none; color: inherit; padding: 0;">${markdown.replace("<", "&lt;").replace(">", "&gt;")}</pre>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
