package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily

data class ToolbarAction(
    val label: String,
    val prefix: String,
    val suffix: String = "",
    val testTag: String
)

@Composable
fun MarkdownToolbar(
    onInsert: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        ToolbarAction("H1", "# ", "", "action_h1"),
        ToolbarAction("H2", "## ", "", "action_h2"),
        ToolbarAction("H3", "### ", "", "action_h3"),
        ToolbarAction("B", "**", "**", "action_bold"),
        ToolbarAction("I", "*", "*", "action_italic"),
        ToolbarAction("~~", "~~", "~~", "action_strike"),
        ToolbarAction("`code`", "`", "`", "action_inline_code"),
        ToolbarAction("```block```", "```\n", "\n```", "action_code_block"),
        ToolbarAction("☑ Task", "- [ ] ", "", "action_task"),
        ToolbarAction("• List", "- ", "", "action_list"),
        ToolbarAction("1. Num", "1. ", "", "action_num_list"),
        ToolbarAction("❝ Quote", "> ", "", "action_quote"),
        ToolbarAction("🔗 Link", "[Link Text](", "https://)", "action_link"),
        ToolbarAction("▦ Table", "| Col 1 | Col 2 |\n| :--- | :--- |\n| Val 1 | Val 2 |", "", "action_table"),
        ToolbarAction("― Rule", "\n---\n", "", "action_divider")
    )

    // Floating Capsule Bar (Apple Dynamic Island / Google M3 Expressive style)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions.forEach { action ->
                    ToolbarPill(
                        action = action,
                        onClick = { onInsert(action.prefix, action.suffix) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarPill(
    action: ToolbarAction,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, label = "pillScale")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(scale)
            .height(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isPressed) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 11.dp)
            .testTag(action.testTag)
    ) {
        Text(
            text = action.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = JetBrainsMonoFamily,
            color = if (isPressed) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
