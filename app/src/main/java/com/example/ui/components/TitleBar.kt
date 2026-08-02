package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun TitleBar(
    activeTab: OpenTab?,
    workspaceName: String,
    colors: EditorColorScheme,
    onOpenCommandPalette: () -> Unit,
    onToggleSidebar: () -> Unit,
    onTogglePanel: () -> Unit,
    onToggleSplit: () -> Unit,
    onNewFile: () -> Unit,
    onNewProject: () -> Unit,
    onRunFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMenu by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp),
        color = colors.activityBarBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo + Desktop Menu Bar Items
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Official VS Code Logo Icon
                Image(
                    painter = painterResource(id = R.drawable.vscode_logo_1785661101448),
                    contentDescription = "VSCode",
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Desktop Menus: File, Edit, Selection, View, Go, Run, Terminal, Help
                val menus = listOf("File", "Edit", "Selection", "View", "Go", "Run", "Terminal", "Help")
                menus.forEach { menuName ->
                    Box {
                        Text(
                            text = menuName,
                            color = if (activeMenu == menuName) colors.activityBarIconSelected else colors.sidebarText,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { activeMenu = if (activeMenu == menuName) null else menuName }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )

                        DropdownMenu(
                            expanded = activeMenu == menuName,
                            onDismissRequest = { activeMenu = null },
                            modifier = Modifier.background(colors.tabInactiveBackground)
                        ) {
                            when (menuName) {
                                "File" -> {
                                    DropdownMenuItem(
                                        text = { Text("New File", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null; onNewFile() },
                                        leadingIcon = { Icon(Icons.Default.Add, null, tint = colors.sidebarText, modifier = Modifier.size(14.dp)) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("New Workspace", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null; onNewProject() },
                                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, tint = colors.sidebarText, modifier = Modifier.size(14.dp)) }
                                    )
                                    HorizontalDivider(color = colors.activityBarBackground)
                                    DropdownMenuItem(
                                        text = { Text("Command Palette... (Ctrl+Shift+P)", color = colors.accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                        onClick = { activeMenu = null; onOpenCommandPalette() }
                                    )
                                }
                                "View" -> {
                                    DropdownMenuItem(
                                        text = { Text("Toggle Side Bar", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null; onToggleSidebar() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Toggle Terminal / Panel", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null; onTogglePanel() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Toggle Split Editor", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null; onToggleSplit() }
                                    )
                                }
                                "Run" -> {
                                    DropdownMenuItem(
                                        text = { Text("Run Without Debugging", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null; onRunFile() },
                                        leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = colors.functionColor, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                                else -> {
                                    DropdownMenuItem(
                                        text = { Text("$menuName Options", color = colors.sidebarText, fontSize = 12.sp) },
                                        onClick = { activeMenu = null }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Center: Command Palette / Quick Search Trigger Box
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .height(24.dp)
                    .clickable { onOpenCommandPalette() },
                shape = RoundedCornerShape(4.dp),
                color = colors.tabInactiveBackground
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.sidebarText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (activeTab != null) "${activeTab.name} — $workspaceName — VSCode" else "$workspaceName — VSCode (Port)",
                            color = colors.sidebarText,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "Ctrl+P",
                        color = colors.editorLineNumber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Window Layout Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onToggleSidebar, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ViewSidebar,
                        contentDescription = "Toggle Sidebar",
                        tint = colors.sidebarText,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(onClick = onTogglePanel, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignBottom,
                        contentDescription = "Toggle Panel",
                        tint = colors.sidebarText,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(onClick = onToggleSplit, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ViewColumn,
                        contentDescription = "Split Editor",
                        tint = colors.sidebarText,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Minimize / Maximize / Close Window Buttons
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), shape = RoundedCornerShape(5.dp)))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF28C840), shape = RoundedCornerShape(5.dp)))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), shape = RoundedCornerShape(5.dp)))
            }
        }
    }
}
