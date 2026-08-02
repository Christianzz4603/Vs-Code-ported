package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodeFileEntity
import com.example.data.local.ProjectEntity
import com.example.ui.theme.EditorColorScheme

@Composable
fun FileExplorer(
    currentProject: ProjectEntity?,
    allProjects: List<ProjectEntity>,
    files: List<CodeFileEntity>,
    colors: EditorColorScheme,
    onProjectSelected: (Long) -> Unit,
    onCreateProject: (String, String, String) -> Unit,
    onFileSelected: (CodeFileEntity) -> Unit,
    onCreateFile: (String, Boolean) -> Unit,
    onDeleteFile: (CodeFileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var isDirectoryCreation by remember { mutableStateOf(false) }
    var showProjectDropdown by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.width(260.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Workspace Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clickable { showProjectDropdown = !showProjectDropdown },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentProject?.name?.uppercase() ?: "NO PROJECT",
                        color = colors.sidebarText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Project",
                        tint = colors.sidebarText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            isDirectoryCreation = false
                            showNewFileDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "New File",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isDirectoryCreation = true
                            showNewFileDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = "New Project",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Project Selector Dropdown
            DropdownMenu(
                expanded = showProjectDropdown,
                onDismissRequest = { showProjectDropdown = false },
                modifier = Modifier.background(colors.tabInactiveBackground)
            ) {
                allProjects.forEach { proj ->
                    DropdownMenuItem(
                        text = { Text(proj.name, color = colors.sidebarText, fontSize = 13.sp) },
                        onClick = {
                            onProjectSelected(proj.id)
                            showProjectDropdown = false
                        }
                    )
                }
                HorizontalDivider(color = colors.activityBarBackground)
                DropdownMenuItem(
                    text = { Text("+ Create New Workspace", color = colors.accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    onClick = {
                        showProjectDropdown = false
                        showNewProjectDialog = true
                    }
                )
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // File Tree List
            if (files.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No files in project.\nTap + to add a file.",
                        color = colors.editorLineNumber,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
                ) {
                    items(files, key = { it.id }) { file ->
                        FileItemRow(
                            file = file,
                            colors = colors,
                            onFileClick = {
                                if (!file.isDirectory) {
                                    onFileSelected(file)
                                }
                            },
                            onDeleteClick = { onDeleteFile(file) }
                        )
                    }
                }
            }
        }
    }

    // New File / Folder Dialog
    if (showNewFileDialog) {
        var nameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text(if (isDirectoryCreation) "New Folder" else "New File", fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text(if (isDirectoryCreation) "e.g. components" else "e.g. Utils.kt") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onCreateFile(nameInput.trim(), isDirectoryCreation)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Project Modal with Template Selector
    if (showNewProjectDialog) {
        var projName by remember { mutableStateOf("") }
        var projDesc by remember { mutableStateOf("") }
        var selectedTemplate by remember { mutableStateOf("compose") }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("New Workspace Project", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text("Project Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = projDesc,
                        onValueChange = { projDesc = it },
                        label = { Text("Description") },
                        singleLine = true
                    )

                    Text("Starter Template:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    TemplateOptionCard("compose", "Android Jetpack Compose", "Kotlin + Compose UI architecture", selectedTemplate) { selectedTemplate = "compose" }
                    TemplateOptionCard("python", "Python AI & Data", "Python script with math & requirements", selectedTemplate) { selectedTemplate = "python" }
                    TemplateOptionCard("web", "HTML / CSS / JS", "Full web application template", selectedTemplate) { selectedTemplate = "web" }
                    TemplateOptionCard("kotlin", "Kotlin CLI Utility", "Console application with Kotlin main", selectedTemplate) { selectedTemplate = "kotlin" }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projName.isNotBlank()) {
                            onCreateProject(projName.trim(), projDesc.trim(), selectedTemplate)
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
                ) {
                    Text("Create Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TemplateOptionCard(
    id: String,
    title: String,
    desc: String,
    selectedId: String,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(6.dp),
        color = if (id == selectedId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = id == selectedId, onClick = onSelect)
            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FileItemRow(
    file: CodeFileEntity,
    colors: EditorColorScheme,
    onFileClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val depth = file.path.count { it == '/' }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFileClick() }
            .padding(start = (12 * (depth + 1)).dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDirectory) Color(0xFFDCB67A) else colors.sidebarText,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = file.name,
                color = colors.sidebarText,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = colors.editorLineNumber,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
