package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
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
import com.example.model.AIMessage
import com.example.model.AISender
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun AIBotPanel(
    messages: List<AIMessage>,
    activeTab: OpenTab?,
    isGenerating: Boolean,
    colors: EditorColorScheme,
    onSendMessage: (String, String) -> Unit,
    onApplyCodeToActiveFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.width(280.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp)
        ) {
            // Panel Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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

            HorizontalDivider(color = colors.activityBarBackground)

            // Quick Prompt Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    ActionChip("💡 Explain", colors) {
                        onSendMessage("Explain active code", "explain")
                    }
                }
                item {
                    ActionChip("🐞 Fix Bugs", colors) {
                        onSendMessage("Fix bugs in active code", "fix")
                    }
                }
                item {
                    ActionChip("⚡ Refactor", colors) {
                        onSendMessage("Refactor active code for clean architecture", "refactor")
                    }
                }
                item {
                    ActionChip("🧪 Unit Tests", colors) {
                        onSendMessage("Generate unit tests for active code", "test")
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
                        onApplyCode = onApplyCodeToActiveFile
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
                                text = "Gemini AI is thinking...",
                                color = colors.editorLineNumber,
                                fontSize = 12.sp
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
                    placeholder = { Text("Ask Gemini AI Copilot...", fontSize = 12.sp, color = colors.editorLineNumber) },
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
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AIMessageBubble(
    message: AIMessage,
    colors: EditorColorScheme,
    onApplyCode: (String) -> Unit
) {
    val isUser = message.sender == AISender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isUser) colors.accentColor else colors.tabInactiveBackground,
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = if (isUser) "You" else "Gemini Copilot",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color.White else colors.functionColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    fontSize = 12.sp,
                    color = if (isUser) Color.White else colors.sidebarText,
                    fontFamily = if (message.content.contains("```")) FontFamily.Monospace else FontFamily.Default
                )

                val extractedCode = extractCodeFromMarkdown(message.content)
                if (!isUser && extractedCode.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onApplyCode(extractedCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.functionColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply to Active File", fontSize = 10.sp, color = Color.Black)
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
