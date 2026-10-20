package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun EditorContent(
    content: String,
    onContentChange: (String) -> Unit,
    showLineNumbers: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lineCount = remember(content) {
        if (content.isEmpty()) 1 else content.lines().size
    }

    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val gutterBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Line Numbers Gutter with Apple/Google editor styling
        if (showLineNumbers) {
            val lineNumbersText = remember(lineCount) {
                (1..lineCount).joinToString("\n")
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(gutterBg)
                    .drawBehind {
                        drawLine(
                            color = dividerColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(top = 16.dp, bottom = 80.dp, end = 8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = lineNumbersText,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.5.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Text Editor Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            if (content.isEmpty()) {
                Text(
                    text = "Write your Markdown here...",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    )
                )
            }
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("markdown_editor_input")
            )
        }
    }
}
