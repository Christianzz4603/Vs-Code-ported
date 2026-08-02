package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Breakpoint
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun RunAndDebugPanel(
    activeTab: OpenTab?,
    breakpoints: List<Breakpoint>,
    colors: EditorColorScheme,
    onStartDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedConfig by remember { mutableStateOf("Run Active File") }
    var isDebugging by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RUN AND DEBUG",
                color = colors.sidebarText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Configuration Dropdown Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.tabInactiveBackground,
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedConfig,
                        color = colors.sidebarText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = colors.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Green Play Debug Button
            Button(
                onClick = {
                    isDebugging = !isDebugging
                    onStartDebug()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDebugging) Color(0xFFE53935) else Color(0xFF388E3C)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(
                    imageVector = if (isDebugging) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDebugging) "Stop Debugging" else "Start Debugging",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Variables Inspector Section
            Text(
                text = "VARIABLES",
                color = colors.editorLineNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.tabInactiveBackground)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("▼ Local Scope", color = colors.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("  this = MainActivity@4f81", color = colors.sidebarText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("  activeTab = ${activeTab?.name ?: "null"}", color = colors.sidebarText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("  isDebugging = $isDebugging", color = colors.sidebarText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Call Stack Section
            Text(
                text = "CALL STACK",
                color = colors.editorLineNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.tabInactiveBackground)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("mainThread (Running)", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("  at executeRunner (${activeTab?.name ?: "Script"}:14)", color = colors.sidebarText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("  at onClick (MainViewModel.kt:182)", color = colors.sidebarText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Breakpoints Section
            Text(
                text = "BREAKPOINTS (${breakpoints.size})",
                color = colors.editorLineNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (breakpoints.isEmpty()) {
                Text(
                    text = "No breakpoints set.\nClick gutter in code editor to add.",
                    color = colors.editorLineNumber,
                    fontSize = 11.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(breakpoints) { bp ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.tabInactiveBackground)
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFE53935), shape = MaterialTheme.shapes.extraSmall)
                            )
                            Text(
                                text = "Line ${bp.lineNumber}",
                                color = colors.sidebarText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
