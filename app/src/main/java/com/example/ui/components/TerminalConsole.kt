package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OpenTab
import com.example.model.PanelTab
import com.example.model.ProblemItem
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import com.example.ui.theme.EditorColorScheme

@Composable
fun IntegratedPanel(
    lines: List<TerminalLine>,
    problems: List<ProblemItem>,
    activeTab: OpenTab?,
    colors: EditorColorScheme,
    onRunCommand: (String) -> Unit,
    onRunActiveFile: () -> Unit,
    onClearTerminal: () -> Unit,
    onClosePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PanelTab.TERMINAL) }
    var commandInput by remember { mutableStateOf("") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // VS Code Panel Header Bar with Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(colors.activityBarBackground)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelTabItem("PROBLEMS (${problems.size})", selectedTab == PanelTab.PROBLEMS, colors) {
                        selectedTab = PanelTab.PROBLEMS
                    }
                    PanelTabItem("OUTPUT", selectedTab == PanelTab.OUTPUT, colors) {
                        selectedTab = PanelTab.OUTPUT
                    }
                    PanelTabItem("DEBUG CONSOLE", selectedTab == PanelTab.DEBUG_CONSOLE, colors) {
                        selectedTab = PanelTab.DEBUG_CONSOLE
                    }
                    PanelTabItem("TERMINAL", selectedTab == PanelTab.TERMINAL, colors) {
                        selectedTab = PanelTab.TERMINAL
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRunActiveFile,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.functionColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(22.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onClearTerminal, modifier = Modifier.size(22.dp)) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(onClick = onClosePanel, modifier = Modifier.size(22.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Panel",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Tab Content Body
            when (selectedTab) {
                PanelTab.TERMINAL -> {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(lines) { line ->
                                val color = when (line.type) {
                                    TerminalLineType.INPUT -> colors.keywordColor
                                    TerminalLineType.OUTPUT -> Color(0xFFE2E8F0)
                                    TerminalLineType.ERROR -> Color(0xFFF87171)
                                    TerminalLineType.SUCCESS -> Color(0xFF4ADE80)
                                    TerminalLineType.SYSTEM -> colors.numberColor
                                }

                                Text(
                                    text = line.text,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Terminal Command Line Prompt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "vscode@android:~$ ",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            BasicTextField(
                                value = commandInput,
                                onValueChange = { commandInput = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    if (commandInput.isNotBlank()) {
                                        onRunCommand(commandInput.trim())
                                        commandInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Run", fontSize = 10.sp)
                            }
                        }
                    }
                }
                PanelTab.PROBLEMS -> {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        if (problems.isEmpty()) {
                            Text("No problems have been detected in the workspace.", color = Color(0xFF4ADE80), fontSize = 11.sp)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(problems) { prob ->
                                    Text(
                                        text = "[Error] ${prob.fileName}:${prob.lineNumber} - ${prob.message}",
                                        color = Color(0xFFF87171),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
                PanelTab.OUTPUT -> {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Text(
                            text = "[VS Code Output Window]\n[Extension Host] Active extension services loaded.\n[Language Server] Kotlin & Compose IntelliSense active.\n[Build System] Ready for compilation.",
                            color = colors.sidebarText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                PanelTab.DEBUG_CONSOLE -> {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Text(
                            text = "> Debug Console ready.\n> Type commands to evaluate expressions in current stack frame.",
                            color = colors.editorLineNumber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PanelTabItem(
    title: String,
    isSelected: Boolean,
    colors: EditorColorScheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) colors.activityBarIconSelected else colors.sidebarText,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}
