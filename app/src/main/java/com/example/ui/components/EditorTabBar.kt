package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun EditorTabBar(
    openTabs: List<OpenTab>,
    activeTabId: Long?,
    colors: EditorColorScheme,
    onTabSelected: (Long) -> Unit,
    onTabClosed: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(36.dp),
        color = colors.tabBarBackground
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(openTabs, key = { it.fileId }) { tab ->
                val isActive = tab.fileId == activeTabId
                val tabBg = if (isActive) colors.tabActiveBackground else colors.tabInactiveBackground

                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(tabBg)
                        .clickable { onTabSelected(tab.fileId) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language icon indicator
                    Text(
                        text = getLanguageBadge(tab.language),
                        color = getBadgeColor(tab.language, colors),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = tab.name,
                        color = if (isActive) colors.sidebarText else colors.editorLineNumber,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                    )

                    if (tab.isDirty) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colors.accentColor, CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tab",
                            tint = colors.editorLineNumber,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onTabClosed(tab.fileId) }
                        )
                    }
                }

                // Divider line between tabs
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.6f)
                        .background(colors.tabBarBackground)
                )
            }
        }
    }
}

private fun getLanguageBadge(language: String): String {
    return when (language.lowercase()) {
        "kotlin" -> "KT"
        "java" -> "JV"
        "python" -> "PY"
        "javascript" -> "JS"
        "typescript" -> "TS"
        "html" -> "<>"
        "css" -> "#"
        "json" -> "{}"
        "markdown" -> "MD"
        "shell" -> "SH"
        else -> "TXT"
    }
}

private fun getBadgeColor(language: String, colors: EditorColorScheme): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFE76F51)
        "python" -> Color(0xFF3572A5)
        "javascript" -> Color(0xFFF7DF1E)
        "typescript" -> Color(0xFF3178C6)
        "html" -> Color(0xFFE34F26)
        "css" -> Color(0xFF563D7C)
        "json" -> Color(0xFF29B6F6)
        else -> colors.functionColor
    }
}
