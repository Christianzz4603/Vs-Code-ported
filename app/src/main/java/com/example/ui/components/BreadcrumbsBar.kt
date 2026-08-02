package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun BreadcrumbsBar(
    activeTab: OpenTab?,
    workspaceName: String,
    colors: EditorColorScheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp),
        color = colors.editorBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = workspaceName,
                color = colors.editorLineNumber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            if (activeTab != null) {
                val segments = activeTab.path.split("/").filter { it.isNotBlank() }

                segments.forEachIndexed { index, segment ->
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.editorLineNumber,
                        modifier = Modifier.size(10.dp)
                    )

                    val isLast = index == segments.size - 1

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (isLast) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = colors.accentColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        Text(
                            text = segment,
                            color = if (isLast) colors.editorTextColor else colors.editorLineNumber,
                            fontSize = 10.sp,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
