package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.model.ActivityTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun ActivityBar(
    activeTab: ActivityTab,
    colors: EditorColorScheme,
    modifiedFilesCount: Int,
    onTabSelected: (ActivityTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(52.dp).fillMaxHeight(),
        color = colors.activityBarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActivityBarItem(
                    icon = Icons.Default.Folder,
                    label = "Explorer",
                    isSelected = activeTab == ActivityTab.EXPLORER,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.EXPLORER) }
                )

                ActivityBarItem(
                    icon = Icons.Default.Search,
                    label = "Search",
                    isSelected = activeTab == ActivityTab.SEARCH,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.SEARCH) }
                )

                ActivityBarItem(
                    icon = Icons.Default.AccountTree,
                    label = "Source Control",
                    isSelected = activeTab == ActivityTab.SOURCE_CONTROL,
                    colors = colors,
                    badgeCount = modifiedFilesCount,
                    onClick = { onTabSelected(ActivityTab.SOURCE_CONTROL) }
                )

                ActivityBarItem(
                    icon = Icons.Default.BugReport,
                    label = "Run & Debug",
                    isSelected = activeTab == ActivityTab.DEBUG,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.DEBUG) }
                )

                ActivityBarItem(
                    icon = Icons.Default.Extension,
                    label = "Extensions",
                    isSelected = activeTab == ActivityTab.EXTENSIONS,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.EXTENSIONS) }
                )

                ActivityBarItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "AI Copilot",
                    isSelected = activeTab == ActivityTab.AI_COPILOT,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.AI_COPILOT) }
                )

                ActivityBarItem(
                    icon = Icons.Default.Terminal,
                    label = "Terminal",
                    isSelected = activeTab == ActivityTab.TERMINAL,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.TERMINAL) }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActivityBarItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = activeTab == ActivityTab.SETTINGS,
                    colors = colors,
                    onClick = { onTabSelected(ActivityTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun ActivityBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    colors: EditorColorScheme,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .align(Alignment.CenterStart)
                    .background(colors.accentColor)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) colors.activityBarIconSelected else colors.activityBarIconUnselected,
            modifier = Modifier.size(24.dp)
        )

        if (badgeCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp),
                containerColor = colors.accentColor,
                contentColor = colors.statusBarText
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
