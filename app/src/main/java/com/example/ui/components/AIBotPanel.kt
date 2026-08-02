package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.EditorColorScheme

@Composable
fun AIBotPanel(
    messages: List<AIMessage>,
    activeTab: OpenTab?,
    isGenerating: Boolean,
    workspaceSummary: String,
    colors: EditorColorScheme,
    onSendMessage: (String, String) -> Unit,
    onApplyCodeToActiveFile: (String) -> Unit,
    onAcceptWorkspaceEdit: (ProposedWorkspaceEdit) -> Unit = {},
    onRejectWorkspaceEdit: (ProposedWorkspaceEdit) -> Unit = {},
    onRollbackWorkspaceEdit: (ProposedWorkspaceEdit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.width(300.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            // Panel Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "GEMINI AI COPILOT",
                        color = colors.sidebarText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "FULL WORKSPACE",
                        color = colors.accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Workspace Index Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = null,
                    tint = colors.functionColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = workspaceSummary,
                    color = colors.editorLineNumber,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Workspace Quick Action Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    ActionChip("🔍 Workspace Analysis", colors) {
                        onSendMessage("Analyze the complete project structure and explain component architecture.", "explain")
                    }
                }
                item {
                    ActionChip("🏗️ Refactor Workspace", colors) {
                        onSendMessage("Refactor project files for clean architecture and better modularity.", "refactor")
                    }
                }
                item {
                    ActionChip("🐞 Fix All Bugs", colors) {
                        onSendMessage("Detect and fix compiler warnings, bugs, and edge cases across all workspace files.", "fix")
                    }
                }
                item {
                    ActionChip("🧪 Generate Tests", colors) {
                        onSendMessage("Generate unit test suites for all main components in the project.", "test")
                    }
                }
                item {
                    ActionChip("🛡️ Security Audit", colors) {
                        onSendMessage("Audit workspace for security issues, memory leaks, and performance bottlenecks.", "audit")
                    }
                }
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Messages History
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    AIMessageBubble(
                        message = msg,
                        colors = colors,
                        onApplyCode = onApplyCodeToActiveFile,
                        onAcceptEdit = onAcceptWorkspaceEdit,
                        onRejectEdit = onRejectWorkspaceEdit,
                        onRollbackEdit = onRollbackWorkspaceEdit
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = colors.accentColor,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Gemini AI indexing workspace & reasoning...",
                                color = colors.editorLineNumber,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ask Gemini AI across workspace...", fontSize = 11.sp, color = colors.editorLineNumber) },
                    textStyle = TextStyle(color = colors.sidebarText, fontSize = 12.sp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            onSendMessage(promptInput.trim(), "chat")
                            promptInput = ""
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = colors.accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, colors: EditorColorScheme, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = colors.tabInactiveBackground
    ) {
        Text(
            text = label,
            color = colors.sidebarText,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AIMessageBubble(
    message: AIMessage,
    colors: EditorColorScheme,
    onApplyCode: (String) -> Unit,
    onAcceptEdit: (ProposedWorkspaceEdit) -> Unit,
    onRejectEdit: (ProposedWorkspaceEdit) -> Unit,
    onRollbackEdit: (ProposedWorkspaceEdit) -> Unit
) {
    val isUser = message.sender == AISender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isUser) colors.accentColor else colors.tabInactiveBackground,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "Gemini Workspace Copilot",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else colors.functionColor
                    )

                    if (!isUser) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = colors.typeColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.content,
                    fontSize = 12.sp,
                    color = if (isUser) Color.White else colors.sidebarText,
                    fontFamily = if (message.content.contains("```")) FontFamily.Monospace else FontFamily.Default
                )

                // Single Code Snippet Quick Apply
                val extractedCode = extractCodeFromMarkdown(message.content)
                if (!isUser && extractedCode.isNotBlank() && message.proposedEdit == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onApplyCode(extractedCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.functionColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply to Active File", fontSize = 10.sp, color = Color.Black)
                    }
                }

                // Multi-File Proposed Workspace Edit Card
                if (message.proposedEdit != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProposedEditCard(
                        edit = message.proposedEdit,
                        colors = colors,
                        onAccept = { onAcceptEdit(message.proposedEdit) },
                        onReject = { onRejectEdit(message.proposedEdit) },
                        onRollback = { onRollbackEdit(message.proposedEdit) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProposedEditCard(
    edit: ProposedWorkspaceEdit,
    colors: EditorColorScheme,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onRollback: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = colors.editorBackground,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = edit.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.sidebarText,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.functionColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${edit.confidenceScore}% Confidence",
                        fontSize = 9.sp,
                        color = colors.functionColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = edit.explanation,
                fontSize = 10.sp,
                color = colors.editorLineNumber
            )

            Spacer(modifier = Modifier.height(6.dp))

            // File changes list summary
            Text(
                text = "Proposed Changes (${edit.fileChanges.size} files):",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.keywordColor
            )

            edit.fileChanges.forEach { change ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (badgeText, badgeColor) = when (change.changeType) {
                        ProposedChangeType.CREATE_FILE -> "NEW" to Color(0xFF4CAF50)
                        ProposedChangeType.DELETE_FILE -> "DEL" to Color(0xFFF44336)
                        ProposedChangeType.RENAME_FILE -> "REN" to Color(0xFFFF9800)
                        ProposedChangeType.EDIT_FILE -> "EDIT" to colors.accentColor
                    }

                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = badgeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = change.filePath,
                        fontSize = 10.sp,
                        color = colors.sidebarText,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Expandable Preview Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isExpanded) "Hide Diff Preview" else "Preview Proposed Code",
                    fontSize = 10.sp,
                    color = colors.accentColor,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isExpanded) {
                edit.fileChanges.forEach { change ->
                    val content = change.newContent
                    if (!content.isNullOrEmpty()) {
                        Text(
                            text = "--- ${change.filePath} ---",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.functionColor
                        )
                        Surface(
                            color = colors.activityBarBackground,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = content.take(200) + if (content.length > 200) "\n..." else "",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.sidebarText,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons based on Status
            when (edit.status) {
                ProposedEditStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Accept All", fontSize = 10.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onReject,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Reject", fontSize = 10.sp, color = colors.sidebarText)
                        }
                    }
                }
                ProposedEditStatus.ACCEPTED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                            Text("Applied to Workspace", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = onRollback,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Undo, contentDescription = "Rollback", tint = colors.warningColor, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                ProposedEditStatus.REJECTED -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = colors.editorLineNumber, modifier = Modifier.size(14.dp))
                        Text("Edits Rejected", fontSize = 10.sp, color = colors.editorLineNumber)
                    }
                }
                ProposedEditStatus.ROLLED_BACK -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = null, tint = colors.warningColor, modifier = Modifier.size(14.dp))
                        Text("Rolled Back", fontSize = 10.sp, color = colors.warningColor)
                    }
                }
            }
        }
    }
}

private fun extractCodeFromMarkdown(markdown: String): String {
    val regex = "```[a-zA-Z]*\\n([\\s\\S]*?)```".toRegex()
    val match = regex.find(markdown)
    return match?.groupValues?.get(1)?.trim() ?: ""
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
