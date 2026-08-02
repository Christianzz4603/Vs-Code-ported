package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.local.CodeFileEntity
import com.example.data.local.GitCommitEntity
import com.example.ui.theme.EditorColorScheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SourceControl(
    modifiedFiles: List<CodeFileEntity>,
    commits: List<GitCommitEntity>,
    colors: EditorColorScheme,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var currentBranch by remember { mutableStateOf("main") }

    Surface(
        modifier = modifier.width(260.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOURCE CONTROL",
                    color = colors.sidebarText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Branch",
                        tint = colors.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = currentBranch,
                        color = colors.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Commit Input Box
            OutlinedTextField(
                value = commitMessage,
                onValueChange = { commitMessage = it },
                placeholder = { Text("Message (Ctrl+Enter to commit)", fontSize = 12.sp, color = colors.editorLineNumber) },
                textStyle = TextStyle(color = colors.sidebarText, fontSize = 12.sp),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            )

            Button(
                onClick = {
                    if (commitMessage.isNotBlank()) {
                        onCommit(commitMessage.trim())
                        commitMessage = ""
                    }
                },
                enabled = modifiedFiles.isNotEmpty() && commitMessage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Commit (${modifiedFiles.size})", fontSize = 12.sp)
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Changes section
            Text(
                text = "CHANGES (${modifiedFiles.size})",
                color = colors.editorLineNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (modifiedFiles.isEmpty()) {
                Text(
                    text = "No uncommitted changes in workspace.",
                    color = colors.editorLineNumber,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(modifiedFiles) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.tabInactiveBackground)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = colors.stringColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = file.name,
                                    color = colors.sidebarText,
                                    fontSize = 12.sp
                                )
                            }
                            Text("M", color = colors.stringColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Commit History Timeline
            Text(
                text = "COMMIT HISTORY",
                color = colors.editorLineNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commits) { c ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.tabInactiveBackground)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = c.message,
                                color = colors.sidebarText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = c.hash,
                                color = colors.accentColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${c.author} • ${dateFormat.format(Date(c.timestamp))}",
                            color = colors.editorLineNumber,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
