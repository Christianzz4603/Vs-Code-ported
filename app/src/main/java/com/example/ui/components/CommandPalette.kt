package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CodeFileEntity
import com.example.model.CommandPaletteItem
import com.example.ui.theme.EditorColorScheme

@Composable
fun CommandPalette(
    isOpen: Boolean,
    files: List<CodeFileEntity>,
    colors: EditorColorScheme,
    onDismiss: () -> Unit,
    onFileSelected: (CodeFileEntity) -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    if (!isOpen) return

    var query by remember { mutableStateOf("> ") }

    val allCommands = remember {
        listOf(
            CommandPaletteItem("cmd_terminal", "View: Toggle Integrated Terminal", "View", "Ctrl+`") { onExecuteCommand("toggle_terminal") },
            CommandPaletteItem("cmd_theme", "Preferences: Color Theme", "Preferences", "Ctrl+K Ctrl+T") { onExecuteCommand("open_settings") },
            CommandPaletteItem("cmd_explain", "Gemini AI: Explain Active Code", "Gemini", "Alt+E") { onExecuteCommand("ai_explain") },
            CommandPaletteItem("cmd_fix", "Gemini AI: Fix Code Errors & Bugs", "Gemini", "Alt+F") { onExecuteCommand("ai_fix") },
            CommandPaletteItem("cmd_run", "Run: Execute Active File Script", "Run", "F5") { onExecuteCommand("run_file") },
            CommandPaletteItem("cmd_commit", "Git: Commit Changes to Repository", "Source Control", "Ctrl+Enter") { onExecuteCommand("git_commit") },
            CommandPaletteItem("cmd_sync_commit", "Git: Sync Commit (Stage, Commit & Push)", "Source Control", "Ctrl+Shift+S") { onExecuteCommand("git_sync") },
            CommandPaletteItem("cmd_new_file", "File: Create New File in Workspace", "File", "Ctrl+N") { onExecuteCommand("new_file") },
            CommandPaletteItem("cmd_split", "View: Split Editor Side-By-Side", "View", "Ctrl+\\") { onExecuteCommand("toggle_split") },
            CommandPaletteItem("cmd_extensions", "View: Show Extension Marketplace", "View", "Ctrl+Shift+X") { onExecuteCommand("open_extensions") }
        )
    }

    val filteredItems = remember(query, files) {
        if (query.startsWith(">")) {
            val filterText = query.removePrefix(">").trim()
            allCommands.filter {
                it.title.contains(filterText, ignoreCase = true) ||
                it.category.contains(filterText, ignoreCase = true)
            }
        } else {
            val filterText = query.trim()
            files.filter { !it.isDirectory && it.name.contains(filterText, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(480.dp)
                .heightIn(max = 380.dp),
            shape = RoundedCornerShape(8.dp),
            color = colors.sidebarBackground,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Command Palette Search Header
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type a command or search files...", fontSize = 12.sp, color = colors.editorLineNumber) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = colors.sidebarText,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = colors.activityBarBackground)

                // Filtered Search Results
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (query.startsWith(">")) {
                        itemsIndexed(filteredItems as List<CommandPaletteItem>) { _, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        item.action()
                                        onDismiss()
                                    }
                                    .background(colors.tabInactiveBackground, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = colors.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = item.title,
                                        color = colors.sidebarText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (item.shortcut != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colors.activityBarBackground
                                    ) {
                                        Text(
                                            text = item.shortcut,
                                            color = colors.editorLineNumber,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(filteredItems as List<CodeFileEntity>) { _, file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onFileSelected(file)
                                        onDismiss()
                                    }
                                    .background(colors.tabInactiveBackground, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = file.name,
                                        color = colors.accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = file.path,
                                        color = colors.editorLineNumber,
                                        fontSize = 10.sp
                                    )
                                }

                                Text(
                                    text = file.language.uppercase(),
                                    color = colors.sidebarText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
