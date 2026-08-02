package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.util.LanguageUtils

@Composable
fun FileExplorer(
    currentProject: ProjectEntity?,
    allProjects: List<ProjectEntity>,
    files: List<CodeFileEntity>,
    colors: EditorColorScheme,
    onProjectSelected: (Long) -> Unit,
    onCreateProject: (String, String, String) -> Unit,
    onFileSelected: (CodeFileEntity) -> Unit,
    onCreateFile: (name: String, path: String, content: String, isDirectory: Boolean) -> Unit,
    onDeleteFile: (CodeFileEntity) -> Unit,
    onRenameFile: (file: CodeFileEntity, newName: String) -> Unit,
    onDuplicateFile: (file: CodeFileEntity) -> Unit,
    onOpenSafFolder: () -> Unit,
    onOpenSafFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showProjectDropdown by remember { mutableStateOf(false) }
    var showHiddenFiles by remember { mutableStateOf(true) }

    var selectedFileForContextMenu by remember { mutableStateOf<CodeFileEntity?>(null) }
    var showPropertiesDialog by remember { mutableStateOf<CodeFileEntity?>(null) }
    var showRenameDialog by remember { mutableStateOf<CodeFileEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedFileIds by remember { mutableStateOf(setOf<Long>()) }

    val filteredFiles = remember(files, showHiddenFiles) {
        if (showHiddenFiles) files else files.filter { !it.name.startsWith(".") }
    }

    Surface(
        modifier = modifier.width(270.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Workspace Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.clickable { showProjectDropdown = !showProjectDropdown },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentProject?.name?.uppercase() ?: "WORKSPACE",
                        color = colors.sidebarText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Workspace",
                        tint = colors.sidebarText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // New File
                    IconButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "New File",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Open SAF Folder
                    IconButton(
                        onClick = onOpenSafFolder,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open Device Folder (SAF)",
                            tint = colors.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Toggle Hidden Files
                    IconButton(
                        onClick = { showHiddenFiles = !showHiddenFiles },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (showHiddenFiles) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Hidden Files",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // New Workspace Project
                    IconButton(
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = "New Workspace",
                            tint = colors.sidebarText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Project Dropdown Menu
            DropdownMenu(
                expanded = showProjectDropdown,
                onDismissRequest = { showProjectDropdown = false },
                modifier = Modifier.background(colors.tabInactiveBackground)
            ) {
                allProjects.forEach { proj ->
                    DropdownMenuItem(
                        text = { Text(proj.name, color = colors.sidebarText, fontSize = 12.sp) },
                        onClick = {
                            onProjectSelected(proj.id)
                            showProjectDropdown = false
                        }
                    )
                }
                HorizontalDivider(color = colors.activityBarBackground)
                DropdownMenuItem(
                    text = { Text("📂 Open Folder from Device (SAF)...", color = colors.accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    onClick = {
                        showProjectDropdown = false
                        onOpenSafFolder()
                    }
                )
                DropdownMenuItem(
                    text = { Text("📄 Open Single File from Device...", color = colors.sidebarText, fontSize = 12.sp) },
                    onClick = {
                        showProjectDropdown = false
                        onOpenSafFile()
                    }
                )
            }

            HorizontalDivider(color = colors.activityBarBackground)

            // Multi-Select Action Bar
            if (isMultiSelectMode) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    color = colors.accentColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${selectedFileIds.size} selected", fontSize = 11.sp, color = colors.sidebarText, fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(
                                onClick = {
                                    val filesToDelete = filteredFiles.filter { selectedFileIds.contains(it.id) }
                                    filesToDelete.forEach { onDeleteFile(it) }
                                    selectedFileIds = emptySet()
                                    isMultiSelectMode = false
                                }
                            ) {
                                Text("Delete Selected", fontSize = 10.sp, color = Color(0xFFE53935))
                            }
                            TextButton(
                                onClick = {
                                    selectedFileIds = emptySet()
                                    isMultiSelectMode = false
                                }
                            ) {
                                Text("Cancel", fontSize = 10.sp, color = colors.sidebarText)
                            }
                        }
                    }
                }
            }

            // File Tree List
            if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = colors.editorLineNumber, modifier = Modifier.size(32.dp))
                        Text(
                            text = "No files in workspace.\nTap + to create any file type or open a device folder.",
                            color = colors.editorLineNumber,
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = onOpenSafFolder,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Open Device Folder (SAF)", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(vertical = 2.dp)
                ) {
                    items(filteredFiles, key = { it.id }) { file ->
                        FileItemRow(
                            file = file,
                            colors = colors,
                            isSelectedInMulti = selectedFileIds.contains(file.id),
                            isMultiSelectMode = isMultiSelectMode,
                            onFileClick = {
                                if (isMultiSelectMode) {
                                    selectedFileIds = if (selectedFileIds.contains(file.id)) selectedFileIds - file.id else selectedFileIds + file.id
                                } else if (!file.isDirectory) {
                                    onFileSelected(file)
                                }
                            },
                            onLongClick = {
                                selectedFileForContextMenu = file
                            },
                            onDeleteClick = { onDeleteFile(file) }
                        )
                    }
                }
            }
        }
    }

    // Context Menu Sheet / Dialog
    selectedFileForContextMenu?.let { file ->
        AlertDialog(
            onDismissRequest = { selectedFileForContextMenu = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val lang = LanguageUtils.detectLanguage(file.name)
                    val iconColor = LanguageUtils.getLanguageIconColor(lang)
                    Box(modifier = Modifier.size(10.dp).background(iconColor, shape = CircleShape))
                    Text(file.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            selectedFileForContextMenu = null
                            if (!file.isDirectory) onFileSelected(file)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📄 Open in Editor", modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            selectedFileForContextMenu = null
                            renameInput = file.name
                            showRenameDialog = file
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✏️ Rename Symbol/File", modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            selectedFileForContextMenu = null
                            onDuplicateFile(file)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋 Duplicate File", modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            selectedFileForContextMenu = null
                            showPropertiesDialog = file
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ℹ️ View File Properties", modifier = Modifier.fillMaxWidth())
                    }

                    TextButton(
                        onClick = {
                            selectedFileForContextMenu = null
                            isMultiSelectMode = true
                            selectedFileIds = setOf(file.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("☑️ Select Multiple", modifier = Modifier.fillMaxWidth())
                    }

                    HorizontalDivider()

                    TextButton(
                        onClick = {
                            selectedFileForContextMenu = null
                            onDeleteFile(file)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗑️ Delete", color = Color(0xFFE53935), modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Rename Dialog
    showRenameDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename ${file.name}", fontSize = 14.sp) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenameFile(file, renameInput.trim())
                            showRenameDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }

    // File Properties Dialog
    showPropertiesDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showPropertiesDialog = null },
            title = { Text("File Properties: ${file.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Path: ${file.path}", fontSize = 11.sp)
                    Text("Language: ${file.language.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Size: ${file.content.length} characters", fontSize = 11.sp)
                    Text("Lines: ${file.content.lines().size} lines", fontSize = 11.sp)
                    Text("Directory: ${if (file.isDirectory) "Yes" else "No"}", fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showPropertiesDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Professional New File Modal
    if (showNewFileDialog) {
        NewFileDialog(
            existingFiles = files,
            colors = colors,
            onDismiss = { showNewFileDialog = false },
            onCreateFile = { name, path, content, isDir ->
                onCreateFile(name, path, content, isDir)
            }
        )
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        var projName by remember { mutableStateOf("") }
        var projDesc by remember { mutableStateOf("") }
        var selectedTemplate by remember { mutableStateOf("compose") }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("New Workspace Project", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
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

                    Text("Template:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = selectedTemplate == "compose", onClick = { selectedTemplate = "compose" }, label = { Text("Android", fontSize = 10.sp) })
                        FilterChip(selected = selectedTemplate == "python", onClick = { selectedTemplate = "python" }, label = { Text("Python", fontSize = 10.sp) })
                        FilterChip(selected = selectedTemplate == "web", onClick = { selectedTemplate = "web" }, label = { Text("Web JS", fontSize = 10.sp) })
                    }
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
                    Text("Create Workspace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FileItemRow(
    file: CodeFileEntity,
    colors: EditorColorScheme,
    isSelectedInMulti: Boolean,
    isMultiSelectMode: Boolean,
    onFileClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val depth = file.path.count { it == '/' }
    val detectedLang = LanguageUtils.detectLanguage(file.name)
    val iconColor = if (file.isDirectory) Color(0xFFDCB67A) else LanguageUtils.getLanguageIconColor(detectedLang)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelectedInMulti) colors.accentColor.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onFileClick() }
            .padding(start = (10 * (depth + 1)).dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelectedInMulti,
                    onCheckedChange = { onFileClick() },
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(iconColor.copy(alpha = 0.2f), shape = RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (file.isDirectory) "D" else file.name.substringAfterLast(".").take(2).uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
            }

            Text(
                text = file.name,
                color = if (file.name.startsWith(".")) colors.editorLineNumber else colors.sidebarText,
                fontSize = 12.sp,
                maxLines = 1,
                fontWeight = if (file.isDirectory) FontWeight.Bold else FontWeight.Normal
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onLongClick, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = colors.editorLineNumber,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
